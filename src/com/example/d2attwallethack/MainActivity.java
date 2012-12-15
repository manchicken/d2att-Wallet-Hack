package com.example.d2attwallethack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.LineIterator;

import com.stericson.RootTools.CommandCapture;
import com.stericson.RootTools.RootTools;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * @author Michael D. Stemle, Jr. (manchicken -AT- notsosoft.net)
 *
 */
public class MainActivity extends Activity {
	protected boolean FAKE_IT = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        this.findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					MainActivity.this.doTheHack();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				}
			}
		});
        
        this.findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					MainActivity.this.undoTheHack();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					MainActivity.this.logIt("Got exception. Poop.");
					e.printStackTrace();
				}
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void logIt(String msg) {
    	TextView log = (TextView) this.findViewById(R.id.textView1);
    	log.append(msg+"\n");
    	
    	return;
    }
    
    private String theFileName() {
    	String real = "build.prop";
    	String fake = "dummy.build.prop";
    	
    	if (FAKE_IT) {
    		return fake;
    	}
    	
    	return real;
    }
    
    private File currentWorkingProps() {
		return new File("/system/"+theFileName());
    }
    
    private String origFile() {
    	return "/system/"+theFileName()+".orig";
    }
    
    private String walletFile() {
    	return theFileName()+".wallet";
    }
    
    
    /**
     * Make a copy of the original build.prop file just in case. This is mostly for development.
     * 
     * @return Returns true or false depending on whether or not the copy succeeded.
     * @throws IOException
     */
    private boolean makeDummyCopy() throws IOException {
    	if (FAKE_IT) {
        	FAKE_IT = false;
    		File src = currentWorkingProps();
    		FAKE_IT = true;
    		File dest = currentWorkingProps();
    		RootTools.copyFile(src.getAbsolutePath(), dest.getAbsolutePath(), true, true);
    	}
    	
    	return true;
    }
    
    private void hackTheFile(InputStream input, OutputStream output) throws IOException {
    	LineIterator li = new LineIterator(new InputStreamReader(input));
    	
//    	Properties props = new Properties();
//    	props.load(input);
//    	
//    	props.setProperty("ro.product.model", "htc_jewel");
//    	props.setProperty("ro.product.name", "htc_jewel");
//    	props.setProperty("ro.product.device", "htc_jewel");
//
//    	props.store(output, null);
    	String line = null;
    	while (li.hasNext()) {
    		line = li.next();
    		if (line.startsWith("ro.product.model")) {
    			line = "ro.product.model=htc_jewel";
    		} else if (line.startsWith("ro.product.name")) {
    			line = "ro.product.name=htc_jewel";
    		} else if (line.startsWith("ro.product.device")) {
    			line = "ro.product.device=htc_jewel";
    		}
    		
    		output.write(line.getBytes());
    		output.write("\n".getBytes());
    	}
    	
    	return;
    }
    
    public void undoTheHack() throws InterruptedException, IOException, TimeoutException {
    	this.logIt("\nVerifying the possibility of the hack...");
    	
    	if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
    	    this.logIt("Yay, root available, gonna start the hack...");
    	} else {
    	    this.logIt("Sorry, no root available... I cannot hack.");
    	    return;
    	}

    	File curr = this.currentWorkingProps();
    	File orig = new File(this.getExternalFilesDir(DOWNLOAD_SERVICE), this.origFile());
    	
    	if (!orig.exists()) {
    		this.logIt("The backup doesn't exist! (I looked here: "+orig.getAbsolutePath()+")");
    	}
    	
    	this.logIt("Copying '"+orig+"' to '"+curr.getAbsolutePath()+"'");
    	RootTools.copyFile(orig.getAbsolutePath(), curr.getAbsolutePath(), true, false);
    	
    	this.fixCurrPerms(curr);
    	
    	this.logIt("Done.");
    }
    
    @SuppressLint("WorldReadableFiles")
	public void doTheHack() throws IOException, InterruptedException, TimeoutException {
    	this.logIt("\nVerifying the possibility of the hack...");
    	
    	if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
    	    this.logIt("Yay, root available, gonna start the hack...");
    	} else {
    	    this.logIt("Sorry, no root available... I cannot hack.");
    	    return;
    	}
    	
    	this.logIt("Remounting /system...");
    	RootTools.remount("/system", "rw");
    	    	
    	makeDummyCopy();
    	
    	File curr = this.currentWorkingProps();
    	File orig = new File(this.getExternalFilesDir(DOWNLOAD_SERVICE), this.origFile());
    	
    	this.logIt("Making backup of '"+curr+"' to orig '"+orig.getAbsolutePath()+"'");
    	RootTools.copyFile(curr.getAbsolutePath(), orig.getAbsolutePath(), true, false);
		
    	File wallet = this.getFileStreamPath(this.walletFile());
    	this.logIt("Opening output stream for wallet: "+wallet);
    	FileOutputStream walletOut = this.openFileOutput(this.walletFile(), MODE_WORLD_READABLE);
    	
    	this.logIt("Opening input stream for current: "+curr);
    	FileInputStream currIn = new FileInputStream(curr);
    	
		this.hackTheFile(currIn, walletOut);		
		
		this.logIt("Making '"+wallet+"' the current file '"+curr+"'");
		RootTools.copyFile(wallet.getAbsolutePath(), curr.getAbsolutePath(), true, false);
		
		this.fixCurrPerms(curr);
		
		this.logIt("Done.");
    }
    
    private void fixCurrPerms(File curr) throws InterruptedException, IOException, TimeoutException {
		CommandCapture command = new CommandCapture(0, "chmod 644 "+curr.getAbsolutePath());
		RootTools.getShell(true).add(command).waitForFinish();
    }
}
