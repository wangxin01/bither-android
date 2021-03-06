/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.exception;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.Thread.UncaughtExceptionHandler;

import net.bither.BitherApplication;
import net.bither.util.SystemUtil;
import android.os.Build;
import android.util.Log;

public class UEHandler implements UncaughtExceptionHandler {

	private String mExternalCacheDir;
	private String appInfo = "";

	public UEHandler() {
		setExternalCacheDir();
	}

	public void setExternalCacheDir() {
		mExternalCacheDir = BitherApplication.mContext.getExternalCacheDir()
				+ File.separator + "bither";
		appInfo = "ver:" + Integer.toString(SystemUtil.getAppVersionCode())
				+ ",sdk:" + Build.VERSION.SDK_INT + ",";
	}

	public void uncaughtException(Thread thread, final Throwable ex) {

		new Thread(new Runnable() {

			public void run() {
				String info = null;
				FileOutputStream fileOutPutStream = null;
				PrintStream printStream = null;
				ByteArrayOutputStream baos = null;
				try {
					baos = new ByteArrayOutputStream();
					File path = new File(mExternalCacheDir);
					if (!path.exists()) {
						path.mkdirs();
					}
					path = new File(path, "error.log");

					fileOutPutStream = new FileOutputStream(path);
					baos.write(appInfo.getBytes());
					printStream = new PrintStream(baos);
					ex.printStackTrace(printStream);
					byte[] data = baos.toByteArray();
					info = new String(data);
					fileOutPutStream.write(data);
					Log.e("UEHandler", info);

					android.os.Process.killProcess(android.os.Process.myPid());

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						if (printStream != null) {
							printStream.close();
						}
						if (fileOutPutStream != null) {
							fileOutPutStream.flush();
							fileOutPutStream.close();
						}
						if (baos != null) {
							baos.close();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		}).start();

	}
}
