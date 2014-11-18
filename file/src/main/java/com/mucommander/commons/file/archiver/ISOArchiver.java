/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.archiver;

import com.github.stephenc.javaisotools.eltorito.impl.ElToritoConfig;
import com.github.stephenc.javaisotools.iso9660.ConfigException;
import com.github.stephenc.javaisotools.iso9660.ISO9660Directory;
import com.github.stephenc.javaisotools.iso9660.ISO9660File;
import com.github.stephenc.javaisotools.iso9660.ISO9660RootDirectory;
import com.github.stephenc.javaisotools.iso9660.impl.CreateISO;
import com.github.stephenc.javaisotools.iso9660.impl.ISO9660Config;
import com.github.stephenc.javaisotools.iso9660.impl.ISOImageFileHandler;
import com.github.stephenc.javaisotools.joliet.impl.JolietConfig;
import com.github.stephenc.javaisotools.rockridge.impl.RockRidgeConfig;
import com.github.stephenc.javaisotools.sabre.HandlerException;
import com.github.stephenc.javaisotools.sabre.StreamHandler;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileAttributes;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Archiver implementation using the ISO9660 archive format.
 *
 * @author Jeppe Vennekilde
 */
public class ISOArchiver extends Archiver{
    private StreamHandler streamHandler;
    private ISO9660Config config;
    private final ISO9660RootDirectory root;
    //Adds support for longer file names & wider range of characters
    private boolean enableJoliet = true;
    //Adds support for deeper directory hierarchies and even bigger file names (up to 255 bytes)
    private boolean enableRockRidge = true;
    //Adds support for creation of bootable iso files (not implemented)
    private boolean enableElTorito = false;

    public ISOArchiver(AbstractFile file) throws FileNotFoundException {
        super(null);
        supporStream = false;
        
        config = new ISO9660Config();
        try {
            config.allowASCII(false);
            config.setInterchangeLevel(1);
            //The rock ridge extension of ISO9660 allow directory depth to exceed 8
            if(enableRockRidge){
                config.restrictDirDepthTo8(false);
            } else {
                config.restrictDirDepthTo8(true);
            }
            config.setPublisher(System.getProperty("user.name"));
            config.setVolumeID(file.getName());
            config.setDataPreparer(System.getProperty("user.name"));
            config.forceDotDelimiter(true);
        } catch (ConfigException ex) {
            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        root = new ISO9660RootDirectory();
        
        try {
            streamHandler = new ISOImageFileHandler(new File(file.getPath()));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /////////////////////////////
    // Archiver implementation //
    /////////////////////////////

    
    @Override
    public OutputStream createEntry(String entryPath, FileAttributes attributes) throws IOException {
        try{
            boolean isDirectory = attributes.isDirectory();
            if(isDirectory){
                String[] split = entryPath.split("\\\\");
                ISO9660Directory dir = new ISO9660Directory(split[split.length-1]);
                getParentDirectory(entryPath).addDirectory(dir);
            } else {
                try {
                    ISO9660File file = new ISO9660File(new File(attributes.getPath()));
                    getParentDirectory(entryPath).addFile(file);
                } catch (HandlerException ex) {
                    Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }catch(RuntimeException e){
            e.printStackTrace();
            throw e;
        }
        return null;
    }
    
    /**
     * Get the ISO9660Directory parent object the path belongs to
     *
     * @param isoPath the sub directory/file of the parent directory it will return
     * @return an ISO9660Directory that is the parent of the provided path
     */
    private ISO9660Directory getParentDirectory(String isoPath){
        String[] directories = isoPath.split("\\\\");
        //Initial directory (root)
        ISO9660Directory parent = root;
        for(int i = 0; i < directories.length - 1; i++){
            ISO9660Directory dir = containsDirectory(parent,directories[i]);
            if(dir == null){
                return null;
            }
            parent = dir;
        }
        return parent;
    }
    
    /**
     * Check if an ISO9660Directory contain a provided sub directory
     *
     * @param parentDirectory the directory that will be searched
     * @param isoSubDirPath the ISO path that will be used for reference to see 
     * if the parent directory contains the sub directory
     * @return an ISO9660Directory that is sub directory of the parent directory
     * null if it does not contain the sub directory
     */
    private ISO9660Directory containsDirectory(ISO9660Directory parentDirectory, String isoSubDirPath){
        for(ISO9660Directory directory : parentDirectory.getDirectories()){
            if(directory.getName().equals(isoSubDirPath)){
                return directory;
            }
        }
        return null;
    }
    
    
    @Override
    public void finish() throws IOException {
        if(root.hasSubDirs() || root.getFiles().size() > 0){
            CreateISO iso = new CreateISO(streamHandler, root);

            RockRidgeConfig rrConfig = null;
            if (enableRockRidge) {
                // Rock Ridge support
                rrConfig = new RockRidgeConfig();
                rrConfig.setMkisofsCompatibility(false);
                rrConfig.hideMovedDirectoriesStore(true);
                rrConfig.forcePortableFilenameCharacterSet(true);
            }

            JolietConfig jolietConfig = null;
            if (enableJoliet) {
                // Joliet support
                jolietConfig = new JolietConfig();
                try {
                    if(config.getPublisher() instanceof String){
                        jolietConfig.setPublisher((String) config.getPublisher());
                    } else {
                        try {
                            jolietConfig.setPublisher((File) config.getPublisher());
                        } catch (HandlerException ex) {
                            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                    jolietConfig.setVolumeID(config.getVolumeID());
                    if(config.getDataPreparer() instanceof String){
                        jolietConfig.setDataPreparer((String) config.getDataPreparer());
                    } else {
                        try {
                            jolietConfig.setDataPreparer((File) config.getDataPreparer());
                        } catch (HandlerException ex) {
                            Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } 
                    jolietConfig.forceDotDelimiter(true);
                } catch (ConfigException ex) {
                    Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            //ELTorito adds support for bootable ISO files, which is not supported at this time
            //As this is for archiving, not creation of bootable ISO files (yet)
            ElToritoConfig elToritoConfig = null;

            try {
                iso.process(config, rrConfig, jolietConfig, elToritoConfig);
            } catch (HandlerException ex) {
                Logger.getLogger(ISOArchiver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
    }
    
}
