package com.frostwire.gui.library;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;

import com.frostwire.gui.player.AudioPlayer;
import com.limegroup.gnutella.FileDesc;
import com.limegroup.gnutella.gui.GUIMediator;
import com.limegroup.gnutella.gui.I18n;
import com.limegroup.gnutella.gui.IconManager;
import com.limegroup.gnutella.gui.tables.LimeTableColumn;
import com.limegroup.gnutella.gui.tables.SizeHolder;
import com.limegroup.gnutella.gui.util.BackgroundExecutorService;

/**
 * This class acts as a single line containing all
 * the necessary Library info.
 * @author Sam Berlin
 */
public final class LibraryFilesTableDataLine extends AbstractLibraryTableDataLine<File> {

    /**
     * Whether or not tooltips will display XML info.
     */
    private static boolean _allowXML;

    /**
     * The schemas available
     */
    private static String[] _schemas;
    
    /**
     * Constant for the column with the icon of the file.
     */
    static final int ICON_IDX = 0;
    
	/**
	 * Constant for the column with the name of the file.
	 */
	static final int NAME_IDX = 1;
	
	/**
	 * Constant for the column storing the size of the file.
	 */
	static final int SIZE_IDX = 2;
	
	/**
	 * Constant for the column storing the file type (extension or more
	 * more general type) of the file.
	 */
	static final int TYPE_IDX = 3;
	
	/**
	 * Constant for the column storing the file's path
	 */
	static final int PATH_IDX = 4;
    
    /**
     * Constant for the column indicating the mod time of a file.
     */
    static final int MODIFICATION_TIME_IDX = 5;
    
    /**
     * Add the columns to static array _in the proper order_.
     * The *_IDX variables above need to match the corresponding
     * column's position in this array.
     */
    private static LimeTableColumn[] ltColumns;

	/** Variable for the name */
	private String _name;

	/** Variable for the type */
	private String _type;

	/** Variable for the size */
	private long _size;
	
	/** Cached SizeHolder */
	private SizeHolder _sizeHolder;

	/** Variable to hold the file descriptor */
	private FileDesc _fileDesc;

	/** Variable for the path */
	private String _path;
	
	/**
	 * The model this is being displayed on
	 */
	private final LibraryFilesTableModel _model;
	
	/**
	 * Whether or not the icon has been loaded.
	 */
	private boolean _iconLoaded = false;
	
	/**
	 * Whether or not the icon has been scheduled to load.
	 */
	private boolean _iconScheduledForLoad = false;

	public LibraryFilesTableDataLine(LibraryFilesTableModel ltm) {
		super();
		_model = ltm;
	}

	public FileDesc getFileDesc() { return _fileDesc; }

	public int getColumnCount() { return getLimeTableColumns().length; }

	/**
	 * Initialize the object.
	 * It will fail if not given a FileDesc or a File
	 * (File is retained for compatability with the Incomplete folder)
	 */
    public void initialize(File file) {
        super.initialize(file);
        
        String fullPath = file.getPath();
        try {
            fullPath = file.getCanonicalPath();
        } catch(IOException ioe) {}
        
		_name = initializer.getName();
		_type = "";
        if (!file.isDirectory()) {
        	//_isDirectory = false;
            int index = _name.lastIndexOf(".");
            int index2 = fullPath.lastIndexOf(File.separator);
            _path = fullPath.substring(0,index2);
            if (index != -1 && index != 0) {
                _type = _name.substring(index+1);
                _name = _name.substring(0, index);
            }
        } else {
        	_path = fullPath;
        	//_isDirectory = true;
        }

        // only load file sizes, do nothing for directories
        // directories implicitly set SizeHolder to null and display nothing
        if( initializer.isFile() ) {
            long oldSize = _size; 
            _size = initializer.length();
            if (oldSize != _size)
                _sizeHolder = new SizeHolder(_size);
        }
    }
    
    void setFileDesc(FileDesc fd) {
        initialize(fd.getFile());
        _fileDesc = fd;
    }
    
    /**
     * Returns the file of this data line.
     */
    public File getFile() {
        return initializer;
    }

	/**
	 * Returns the object stored in the specified cell in the table.
	 *
	 * @param idx  The column of the cell to access
	 *
	 * @return  The <code>Object</code> stored at the specified "cell" in
	 *          the list
	 */
	public Object getValueAt(int idx) {
		boolean isPlaying = isPlaying();
	    switch (idx) {
	    case ICON_IDX:
	    	boolean iconAvailable = IconManager.instance().isIconForFileAvailable(initializer);
	        if(!iconAvailable && !_iconScheduledForLoad) {
	            _iconScheduledForLoad = true;
                BackgroundExecutorService.schedule(new Runnable() {
                    public void run() {
                        GUIMediator.safeInvokeAndWait(new Runnable() {
                            public void run() {
                                IconManager.instance().getIconForFile(initializer);
                                _iconLoaded = true;
                                _model.refresh();
                            }
                        });
                    }
                });
	            return null;
            } else if(_iconLoaded || iconAvailable) {
	            return IconManager.instance().getIconForFile(initializer);
            } else {
                return null;
            }
	    case NAME_IDX:
	        return new PlayableCell(_name, isPlaying);	                    
	    case SIZE_IDX:
	        return _sizeHolder == null ? "" : new PlayableCell(_sizeHolder.toString(),isPlaying);
	    case TYPE_IDX:
	        return new PlayableCell(_type, isPlaying);
	    case PATH_IDX:
	        return new PlayableCell(_path, isPlaying);
        case MODIFICATION_TIME_IDX:
			// it's cheaper to use the cached value if available,
			// hope it's always uptodate
			if (_fileDesc != null) {
				return new PlayableCell(new Date(_fileDesc.lastModified()),isPlaying);
			}
			return new PlayableCell(new Date(initializer.lastModified()),isPlaying);
	    }
	    return null;
	}

	private boolean isPlaying() {
		if (initializer != null) {
			return AudioPlayer.instance().isThisBeingPlayed(
					initializer);
		}

		return false;
	}

	public LimeTableColumn getColumn(int idx) {
	    return getLimeTableColumns()[idx];
	}
	
	public boolean isClippable(int idx) {
	    switch(idx) {
        case ICON_IDX:
            return false;
        default:
            return true;
        }
    }
    
    public int getTypeAheadColumn() {
        return NAME_IDX;
    }

	public boolean isDynamic(int idx) {
	    return false;
	}

	public String[] getToolTipArray(int col) {
		return new String[] {""};
	}
	
	private LimeTableColumn[] getLimeTableColumns() {
	    if (ltColumns == null) {
	        LimeTableColumn[] temp =
	        {
	            new LimeTableColumn(ICON_IDX, "LIBRARY_TABLE_ICON", I18n.tr("Icon"),
	                    GUIMediator.getThemeImage("question_mark"), 18, true, Icon.class),
	            
	            new LimeTableColumn(NAME_IDX, "LIBRARY_TABLE_NAME", I18n.tr("Name"),
	                    239, true, PlayableCell.class),
	            
	            new LimeTableColumn(SIZE_IDX, "LIBRARY_TABLE_SIZE", I18n.tr("Size"),
	                    62, true, PlayableCell.class),

	            new LimeTableColumn(TYPE_IDX, "LIBRARY_TABLE_TYPE", I18n.tr("Type"),
	                    48, true, PlayableCell.class),
	                                                    
	            new LimeTableColumn(PATH_IDX, "LIBRARY_TABLE_PATH", I18n.tr("Path"),
	                    108, true, PlayableCell.class),

	            new LimeTableColumn(MODIFICATION_TIME_IDX, 
	                    "LIBRARY_TABLE_MODIFICATION_TIME", I18n.tr("Last Modified"),
	                    20, false, PlayableCell.class),
	        };
	        ltColumns = temp;
	    }
	    return ltColumns;
	}
}
