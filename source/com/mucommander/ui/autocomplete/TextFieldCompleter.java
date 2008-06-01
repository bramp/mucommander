package com.mucommander.ui.autocomplete;

import javax.swing.text.BadLocationException;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * TextFieldCompleter is a CompleterType which suite to text-field.
 * 
 * @author Arik Hadas, based on the code of Santhosh Kumar: http://www.jroller.com/santhosh/entry/file_path_autocompletion
 */

public class TextFieldCompleter extends AutoCompletionType {
	
    private class ShowingThreadImp extends ShowingThread {
    	public ShowingThreadImp(int delay) {
    		super(delay);
    	}

		void showPopup() {
	        if (autocompletedtextComp.isShowing() && autocompletedtextComp.isEnabled() && updateListData(list) && list.getModel().getSize()!=0){
					            
	            list.setVisibleRowCount(Math.min(list.getModel().getSize() ,VISIBLE_ROW_COUNT));
	            
	            int x = 0; 
	            try{	                
	                x = autocompletedtextComp.modelToView().x;
	            } catch(BadLocationException e){ 
	                // this should never happen!!! 
	                e.printStackTrace(); 
	                return;
	            }
	            if (autocompletedtextComp.hasFocus()) {	            	
	            	if (!isStopped) {
	            		list.ensureIndexIsVisible(0);
	            		synchronized(popup) {
		            		popup.show(autocompletedtextComp.getTextComponent(), x, autocompletedtextComp.getHeight());
		            		
		            		// probably because of swing's bug, sometimes the popup window looks
		            		// as a gray rectangle - repainting solves it.
		            		popup.repaint();
	            		}
	            	}
	            }	            
	        }
		}
    }
        
    public TextFieldCompleter(AutocompleterTextComponent comp, Completer completer){
    	super(comp, completer);

    	autocompletedtextComp.getDocument().addDocumentListener(documentListener);

        autocompletedtextComp.addKeyListener(new KeyAdapter() {
        	public void keyPressed(KeyEvent keyEvent) {        		
                
                switch (keyEvent.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                	if (isItemSelectedAtPopupList()) {
                		hidePopup();
                		acceptListItem((String)list.getSelectedValue());
                		keyEvent.consume();
                	}
                	else
                		autocompletedtextComp.OnEnterPressed(keyEvent);
                	break;
                case KeyEvent.VK_ESCAPE:
                	if (isPopupListShowing()) {
                		if (autocompletedtextComp.isEnabled())
                			hidePopup();
                		keyEvent.consume();
                	}
                	else
                		autocompletedtextComp.OnEscPressed(keyEvent);
                	break;
                case KeyEvent.VK_UP:
                	if(autocompletedtextComp.isEnabled() && popup.isVisible()) {
                		selectPreviousPossibleValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_DOWN:
                	if(autocompletedtextComp.isEnabled()){ 
                        if(popup.isVisible()) {
                            selectNextPossibleValue();
                            keyEvent.consume();
                        }
                        else {                	
                        	autocompletedtextComp.moveCarentToEndOfText();
                    		createNewShowingThread(0);
                        }
                    }
                	break;
                case KeyEvent.VK_PAGE_DOWN:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectNextPage();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_PAGE_UP:
                	if(autocompletedtextComp.isEnabled() && isItemSelectedAtPopupList()) {
                		selectPreviousPage();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_HOME:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectFirstValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_END:
                	if(autocompletedtextComp.isEnabled() && isPopupListShowing()) {
                		selectLastValue();
                		keyEvent.consume();
                	}
                	break;
                case KeyEvent.VK_LEFT:
                	hidePopup();
                	break;
                default:
                }
            }
        });
    }
            
    protected void startNewShowingThread(int delay) {    	    	
    	(showingThread = new ShowingThreadImp(delay)).start();
    }
        
    protected void hidePopup() {
		synchronized (popup) {
			if (popup.isVisible())
        		popup.setVisible(false);
		}
	}
}