/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2008 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.file.util;

import com.sun.jna.Native;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.LongByReference;

/**
 * Exposes parts of the Windows Kernel32 API using JNA (Java Native Access).
 *
 * @author Maxence Bernard
 */
public interface Kernel32API extends W32API {

    /** Custom alignment of structures. */
    int STRUCTURE_ALIGNMENT = Structure.ALIGN_NONE;

    /** An instance of the Kernel32 DLL */
    Kernel32API INSTANCE = (Kernel32API) Native.loadLibrary("kernel32", Kernel32API.class, DEFAULT_OPTIONS);

    
    ///////////////////////////
    // SetErrorMode Function //
    ///////////////////////////

    /** Use the system default, which is to display all error dialog boxes. */
    public int SEM_DEFAULT = 0;
    /** The system does not display the critical-error-handler message box. Instead, the system sends the error to the
     *  calling process. */
    public int SEM_FAILCRITICALERRORS = 0x0001;
    /** The system automatically fixes memory alignment faults and makes them invisible to the application. It does this
     *  for the calling process and any descendant processes. This feature is only supported by certain processor
     *  architectures. For more information, see the Remarks sections. After this value is set for a process, subsequent
     *  attempts to clear the value are ignored. */
    public int SEM_NOALIGNMENTFAULTEXCEPT = 0x0004;
    /** The system does not display the general-protection-fault message box. This flag should only be set by debugging
     *  applications that handle general protection (GP) faults themselves with an exception handler. */
    public int SEM_NOGPFAULTERRORBOX = 0x0002;
    /** The system does not display a message box when it fails to find a file. Instead, the error is returned to the
     *  calling process. */
    public int SEM_NOOPENFILEERRORBOX = 0x8000;

    /**
     * Controls whether the system will handle the specified types of serious errors or whether the process will handle
     * them.
     *
     * <p>Remarks: Each process has an associated error mode that indicates to the system how the application is going
     * to respond to serious errors. A child process inherits the error mode of its parent process. To retrieve the
     * process error mode, use the GetErrorMode function.</br>
     * Because the error mode is set for the entire process, you must ensure that multi-threaded applications do not set
     * different error-mode flags. Doing so can lead to inconsistent error handling.</br>
     * The system does not make alignment faults visible to an application on all processor architectures. Therefore,
     * specifying SEM_NOALIGNMENTFAULTEXCEPT is not an error on such architectures, but the system is free to silently
     * ignore the request.
     *
     * @param uMode The process error mode. This parameter can be one or more of the following values:
     * SEM_DEFAULT (alone), SEM_FAILCRITICALERRORS, SEM_NOALIGNMENTFAULTEXCEPT, SEM_NOGPFAULTERRORBOX and
     * SEM_NOOPENFILEERRORBOX.
     * @return the previous state of the error-mode bit flags.
     */
    int SetErrorMode(int uMode);


    /////////////////////////////////
    // GetDiskFreeSpaceEx function //
    /////////////////////////////////

    /**
     * Retrieves information about the amount of space that is available on a disk volume, which is the total amount of
     * space, the total amount of free space, and the total amount of free space available to the user that is 
     * associated with the calling thread.
     *
     * @param lpDirectoryName A directory on the disk.
     * If this parameter is NULL, the function uses the root of the current disk.
     * If this parameter is a UNC name, it must include a trailing backslash, for example, "\\MyServer\MyShare\".
     * This parameter does not have to specify the root directory on a disk. The function accepts any directory on a disk.
     * The calling application must have FILE_LIST_DIRECTORY access rights for this directory.
     * @param lpFreeBytesAvailable A pointer to a variable that receives the total number of free bytes on a disk that
     * are available to the user who is associated with the calling thread. This parameter can be NULL.
     * If per-user quotas are being used, this value may be less than the total number of free bytes on a disk.
     * @param lpTotalNumberOfBytes A pointer to a variable that receives the total number of bytes on a disk that are
     * available to the user who is associated with the calling thread. This parameter can be NULL.
     * If per-user quotas are being used, this value may be less than the total number of bytes on a disk.
     * To determine the total number of bytes on a disk or volume, use IOCTL_DISK_GET_LENGTH_INFO.
     * @param lpTotalNumberOfFreeBytes A pointer to a variable that receives the total number of free bytes on a disk.
     * This parameter can be NULL.
     * @return If the function succeeds, the return value is nonzero. If the function fails, the return value is
     * zero (0). To get extended error information, call GetLastError.
     */
    boolean GetDiskFreeSpaceEx(String lpDirectoryName,
    		LongByReference lpFreeBytesAvailable,
    		LongByReference lpTotalNumberOfBytes,
    		LongByReference lpTotalNumberOfFreeBytes);


    ///////////////////////////
    // GetDriveType function //
    ///////////////////////////

    /** The drive type cannot be determined. */
    public final static int DRIVE_UNKNOWN = 0;

    /** The root path is invalid; for example, there is no volume is mounted at the path. */
    public final static int DRIVE_NO_ROOT_DIR = 1;

    /** The drive has removable media; for example, a floppy drive, thumb drive, or flash card reader. */
    public final static int DRIVE_REMOVABLE = 2;

    /** The drive has fixed media; for example, a hard drive or flash drive. */
    public final static int DRIVE_FIXED = 3;

    /** The drive is a remote (network) drive. */
    public final static int DRIVE_REMOTE = 4;

    /** The drive is a CD-ROM drive. */
    public final static int DRIVE_CDROM = 5;

    /** The drive is a RAM disk. */
    public final static int DRIVE_RAMDISK = 6;

    /**
     * Determines whether a disk drive is a removable, fixed, CD-ROM, RAM disk, or network drive.
     * 
     * @param lpRootPathName The root directory for the drive. A trailing backslash is required. If this parameter is
     * NULL, the function uses the root of the current directory.
     * @return The return value specifies the type of drive, which can be one of the above values.
     */
    int GetDriveType(String lpRootPathName);


    /////////////////////////
    // MoveFileEx function //
    /////////////////////////

    /** If a file named lpNewFileName exists, the function replaces its contents with the contents of the
     * lpExistingFileName file. This value cannot be used if lpNewFileName or lpExistingFileName names a directory. */
    public final static int MOVEFILE_REPLACE_EXISTING = 1;

    /** If the file is to be moved to a different volume, the function simulates the move by using the CopyFile and
     * DeleteFile functions.<br/>
     * This value cannot be used with MOVEFILE_DELAY_UNTIL_REBOOT. */
    public final static int MOVEFILE_COPY_ALLOWED = 2;

    /** The system does not move the file until the operating system is restarted. The system moves the file immediately
     * after AUTOCHK is executed, but before creating any paging files. Consequently, this parameter enables the
     * function to delete paging files from previous startups.<br/>
     * This value can be used only if the process is in the context of a user who belongs to the administrators group or
     * the LocalSystem account.<br/>
     * This value cannot be used with MOVEFILE_COPY_ALLOWED.<br/>
     * <b>Windows 2000</b>:  If you specify the MOVEFILE_DELAY_UNTIL_REBOOT flag for dwFlags, you cannot also prepend
     * the filename that is specified by lpExistingFileName with "\\?". */
    public final static int MOVEFILE_DELAY_UNTIL_REBOOT = 4;

    /** The function does not return until the file is actually moved on the disk.<br/>
     * Setting this value guarantees that a move performed as a copy and delete operation is flushed to disk before the
     * function returns. The flush occurs at the end of the copy operation.<br/>
     * This value has no effect if MOVEFILE_DELAY_UNTIL_REBOOT is set. */
    public final static int MOVEFILE_WRITE_THROUGH = 8;

    /** Reserved for future use. */
    public final static int MOVEFILE_CREATE_HARDLINK = 16;

    /** The function fails if the source file is a link source, but the file cannot be tracked after the move.
     * This situation can occur if the destination is a volume formatted with the FAT file system. */
    public final static int MOVEFILE_FAIL_IF_NOT_TRACKABLE = 32;

    /**
     * Moves an existing file or directory, including its children, with various move options.
     *
     * <p><b>Warning</b>: this method is NOT available on Windows 95, 98 and Me.</p>
     * 
     * @param lpExistingFileName The current name of the file or directory on the local computer. If dwFlags specifies
     * MOVEFILE_DELAY_UNTIL_REBOOT, the file cannot exist on a remote share, because delayed operations are performed
     * before the network is available.
     * @param lpNewFileName The new name of the file or directory on the local computer. When moving a file, the
     * destination can be on a different file system or volume. If the destination is on another drive, you must set the
     * MOVEFILE_COPY_ALLOWED flag in dwFlags. When moving a directory, the destination must be on the same drive.
     * @param dwFlags This parameter can be one or more of the 'MOVEFILE_' constant values. 
     * @return If the function succeeds, the return value is nonzero. If the function fails, the return value is
     * zero (0). To get extended error information, call GetLastError.
     */
    boolean MoveFileEx(String lpExistingFileName, String lpNewFileName, int dwFlags);
}
