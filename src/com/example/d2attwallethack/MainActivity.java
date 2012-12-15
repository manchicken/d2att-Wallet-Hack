package com.example.d2attwallethack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

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
	protected boolean FAKE_IT = true;

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
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        this.findViewById(R.id.button2).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.this.undoTheHack();
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
    	return theFileName()+".orig";
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
    	Properties props = new Properties();
    	props.load(input);
    	
    	props.setProperty("ro.product.model", "htc_jewel");
    	props.setProperty("ro.product.name", "htc_jewel");
    	props.setProperty("ro.product.device", "htc_jewel");

    	props.store(output, null);
    }
    
    public void undoTheHack() {
    	this.logIt("\nVerifying the possibility of the hack...");
    	
    	if (RootTools.isRootAvailable() && RootTools.isAccessGiven()) {
    	    this.logIt("Yay, root available, gonna start the hack...");
    	} else {
    	    this.logIt("Sorry, no root available... I cannot hack.");
    	    return;
    	}

    	File curr = this.currentWorkingProps();
    	File orig = new File(this.getExternalFilesDir(null), this.origFile());
    	
    	if (!orig.exists()) {
    		this.logIt("The backup doesn't exist! (I looked here: "+orig+")");
    	}
    	
    	this.logIt("Copying '"+orig+"' to '"+curr+"'");
    	RootTools.copyFile(orig.getAbsolutePath(), curr.getAbsolutePath(), true, false);
    	
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
    	File orig = new File(this.getExternalFilesDir(null), this.origFile());
    	
    	this.logIt("Making backup of '"+curr+"' to orig '"+orig+"'");
    	RootTools.copyFile(curr.getAbsolutePath(), orig.getAbsolutePath(), true, false);
		
    	File wallet = this.getFileStreamPath(this.walletFile());
    	this.logIt("Opening output stream for wallet: "+wallet);
    	FileOutputStream walletOut = this.openFileOutput(this.walletFile(), MODE_WORLD_READABLE);
    	
    	this.logIt("Opening input stream for current: "+curr);
    	FileInputStream currIn = new FileInputStream(curr);
    	
		this.hackTheFile(currIn, walletOut);		
		
		this.logIt("Making '"+wallet+"' the current file '"+curr+"'");
		RootTools.copyFile(wallet.getAbsolutePath(), curr.getAbsolutePath(), true, true);
		
		this.logIt("Done.");
    }
}
