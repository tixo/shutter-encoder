/*******************************************************************************************
* Copyright (C) 2020 PACIFICO PAUL
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License along
* with this program; if not, write to the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
* 
********************************************************************************************/

package functions.video;

import java.awt.Cursor;
import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;



import application.Ftp;
import application.VideoPlayer;
import application.OverlayWindow;
import application.WatermarkWindow;
import application.Settings;
import application.Shutter;
import application.SubtitlesWindow;
import application.Utils;
import application.Wetransfer;
import library.FFMPEG;
import library.FFPROBE;

public class QTAnimation extends Shutter {
	
	
	
	private static int complete;
	private static File vidstab = null;
	
	public static void main() {

		Thread thread = new Thread(new Runnable(){			
			@Override
			public void run() {
				
				if (scanIsRunning == false)
					complete = 0;
				
				lblTermine.setText(Utils.fichiersTermines(complete));

				for (int i = 0 ; i < liste.getSize() ; i++)
				{
					File file = new File(liste.getElementAt(i));
					
					//SCANNING
		            if (Shutter.scanIsRunning)
		            {
		            	file = Utils.scanFolder(liste.getElementAt(i));
		            	if (file != null)
		            		btnStart.setEnabled(true);
		            	else
		            		break;
		            	Shutter.progressBar1.setIndeterminate(false);		
		            }
		            else if (Settings.btnWaitFileComplete.isSelected())
		            {
						progressBar1.setIndeterminate(true);
						lblEncodageEnCours.setForeground(Color.LIGHT_GRAY);
						lblEncodageEnCours.setText(file.getName());
						tempsRestant.setVisible(false);
						btnStart.setEnabled(false);
						btnAnnuler.setEnabled(true);
						comboFonctions.setEnabled(false);
						
						long fileSize = 0;
						do {
							fileSize = file.length();
							try {
								Thread.sleep(3000);
							} catch (InterruptedException e) {} // Permet d'attendre la nouvelle valeur de la copie
						} while (fileSize != file.length() && cancelled == false);

						// pour Windows
						while (file.renameTo(file) == false && cancelled == false) {
							if (file.exists() == false) // Dans le cas où on annule la copie en cours
								break;
							try {
								Thread.sleep(10);
							} catch (InterruptedException e) {
							}
						}
						
						if (cancelled)
						{
							progressBar1.setIndeterminate(false);
							lblEncodageEnCours.setText(language.getProperty("lblEncodageEnCours"));
							btnStart.setEnabled(true);
							btnAnnuler.setEnabled(false);
							comboFonctions.setEnabled(true);
							break;
						}
						
						progressBar1.setIndeterminate(false);
						btnAnnuler.setEnabled(false);
		            }
		           //SCANNING
										
				try {
					
					String fichier = file.getName();
					final String extension =  fichier.substring(fichier.lastIndexOf("."));
					
					//Audio
					String audio = "";
					
					//caseOPATOM
					if (caseOPATOM.isSelected())
					{
						frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						
		            	String audioFiles = "";
						//On cherche le fichier vidéo
			            if (FFPROBE.FindStreams(file.toString()))
			            	audioFiles = setAudioFiles(audioFiles, file);
			            else
			            	continue;
			            
			            audio = audioFiles;
			            
			            frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
					}						
					
					lblEncodageEnCours.setText(fichier);
					
					//Analyse des données
					if (analyse(file) == false)
						continue;	
					
					String concat = "";
					//Traitement de la file en Bout à bout
					if (Settings.btnSetBab.isSelected())
					{
						file = setBAB(fichier, extension);	
						if (caseActiverSequence.isSelected() == false)
						concat = " -safe 0 -f concat";
					}
					
					audio = setAudio(audio);				
					
					//InOut	
					FFMPEG.fonctionInOut();	
					 
					//Séquence d'images
					String sequence = setSequence(file, extension);
					
					file = setSequenceName(file, extension);
					
					//Loop image					
					String loop = setLoop(extension);
					
	            	//Logo
			        String logo = setLogo();	
					
					//Subtitles
					String subtitles = setSubtitles();
					
					//Interlace
		            String forceField = setInterlace();	
		            
		            //Colorspace
		            String colorspace = setColorspace();
										
			        //Deinterlace
					String filterComplex = setDeinterlace();	
										
					//Blend
					filterComplex = setBlend(filterComplex);
					
					//MotionBlur
					filterComplex = setMotionBlur(filterComplex);
					
					//LUTs
					filterComplex = setLUT(filterComplex);
							 
					//Color
					filterComplex = setColor(filterComplex);
					           
					//Stabilisation
					filterComplex = setStabilisation(vidstab, filterComplex, file, fichier, concat);
							
					//Deflicker
					filterComplex= setDeflicker(filterComplex);
					
					//Deband
					filterComplex = setDeband(filterComplex);					
					
					//Détails
					filterComplex = setDetails(filterComplex);				
					
					//Bruit
					filterComplex = setDenoiser(filterComplex);
					
					//Exposure
					filterComplex = setExposure(filterComplex);
					
					//Interpolation
					filterComplex = setInterpolation(filterComplex);
					
					//Ralenti
					filterComplex = setSlowMotion(filterComplex);
					
			        //PTS
					filterComplex = setPTS(filterComplex);
					
					//Conformisation
					filterComplex = setConform(filterComplex);
					
	            	//Timecode
					filterComplex = showTimecode(filterComplex, fichier);	
					
			    	//Watermak
					filterComplex = setWatermark(filterComplex);
					
			    	//Rognage
					filterComplex = setCrop(filterComplex);
					
					//Rotate
					filterComplex = setRotate(filterComplex);
					
			        //DAR
					filterComplex = setDAR(filterComplex);
					
					//Padding
					filterComplex = setPad(filterComplex);
					
					//Interlace50p
		            filterComplex = setInterlace50p(filterComplex);	
					
					//Overlay
					filterComplex = setOverlay(filterComplex);
					
					//Limiter
					filterComplex = setLimiter(filterComplex);
					
	            	//filterComplex
					filterComplex = setFilterComplex(filterComplex, audio);	
					
	            	//Timecode
					String timecode = setTimecode();
					
		            //Flags
		    		String flags = setFlags();
		    		
		    		//OPATOM
		    		String opatom = setOPATOM(audio);
					
					//Framerate
					String frameRate = setFramerate();	
					
					//Dossier de sortie
					String sortie = setSortie("", file);
					
					//Fichier de sortie
					String nomExtension;
					if (Settings.btnExtension.isSelected())
						nomExtension = Settings.txtExtension.getText();
					else		
						nomExtension =  "_QT_Animation";
		            
		            //Sortie
					String sortieFichier =  sortie.replace("\\", "/") + "/" + fichier.replace(extension, nomExtension + ".mov");
		            														
					//Si le fichier existe
					File fileOut = new File(sortieFichier);
					if(fileOut.exists())
					{						
						fileOut = Utils.fileReplacement(sortie, fichier, extension, nomExtension + "_", ".mov");
						if (fileOut == null)
							continue;						
					}					
					
					String output = '"' + fileOut.toString() + '"';
					/*if (caseVisualiser.isSelected())						
						output = "-f tee " + '"' + fileOut.toString().replace("\\", "/") + "|[f=matroska]pipe:play" + '"';*/
						
					if (cancelled == false && FFMPEG.error == false)
					{
						//Envoi de la commande
						String cmd = opatom + frameRate + filterComplex + " -c:v qtrle" + colorspace + forceField + timecode + flags + " -y ";
						if (caseStabilisation.isSelected())
						{
							String stab = " PathToFFMPEG -i pipe:stab" + cmd;
							FFMPEG.run(FFMPEG.inPoint + concat + " -i " + '"' + file.toString() + '"' + logo + subtitles + FFMPEG.postInPoint + FFMPEG.outPoint + " -pix_fmt yuv420p -f yuv4mpegpipe pipe:stab |" + stab + output);
						}
						else
							FFMPEG.run(loop + FFMPEG.inPoint + sequence + concat + " -i " + '"' + file.toString() + '"' + logo + subtitles + FFMPEG.postInPoint + FFMPEG.outPoint + cmd + output);		
									
						//Attente de la fin de FFMPEG
						do
								Thread.sleep(100);
						while(FFMPEG.runProcess.isAlive());
					}
					
					if (FFMPEG.saveCode == false && btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false 
					|| FFMPEG.saveCode == false && caseActiverSequence.isSelected()
					|| FFMPEG.saveCode == false && Settings.btnSetBab.isSelected())
					{
						if (actionsDeFin(fichier, fileOut, sortie, vidstab))
							break;
					}
				} catch (InterruptedException e) {
					FFMPEG.error  = true;
				}//End Try
			}//End For					
				
				if (btnStart.getText().equals(Shutter.language.getProperty("btnAddToRender")) == false)
					FinDeFonction();
			}//run
			
		});
		thread.start();
		
    }//main

	protected static File setBAB(String fichier, String extension) {
		
		String sortie =  new File(liste.getElementAt(0)).getParent();
		
		if (caseChangeFolder1.isSelected())
			sortie = lblDestination1.getText();
			
		File listeBAB = new File(sortie.replace("\\", "/") + "/" + fichier.replace(extension, ".txt")); 
		
		try {			
			int dureeTotale = 0;
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));			
			PrintWriter writer = new PrintWriter(listeBAB, "UTF-8");      
			
			for (int i = 0 ; i < liste.getSize() ; i++)
			{				
				//Scanning
				if (Settings.btnWaitFileComplete.isSelected())
	            {
					File file = new File(liste.getElementAt(i));
					
					progressBar1.setIndeterminate(true);
					lblEncodageEnCours.setForeground(Color.LIGHT_GRAY);
					lblEncodageEnCours.setText(file.getName());
					tempsRestant.setVisible(false);
					btnStart.setEnabled(false);
					btnAnnuler.setEnabled(true);
					comboFonctions.setEnabled(false);
					
					long fileSize = 0;
					do {
						fileSize = file.length();
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {} // Permet d'attendre la nouvelle valeur de la copie
					} while (fileSize != file.length() && cancelled == false);

					// pour Windows
					while (file.renameTo(file) == false && cancelled == false) {
						if (file.exists() == false) // Dans le cas où on annule la copie en cours
							break;
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
						}
					}
					
					if (cancelled)
					{
						progressBar1.setIndeterminate(false);
						lblEncodageEnCours.setText(language.getProperty("lblEncodageEnCours"));
						btnStart.setEnabled(true);
						btnAnnuler.setEnabled(false);
						comboFonctions.setEnabled(true);
						break;
					}
					
					progressBar1.setIndeterminate(false);
					btnAnnuler.setEnabled(false);
	            }
				//Scanning
				
				FFPROBE.Data(liste.getElementAt(i));
				do {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {}
				} while (FFPROBE.isRunning == true);
				dureeTotale += FFPROBE.dureeTotale;
				
				writer.println("file '" + liste.getElementAt(i) + "'");
			}				
			writer.close();
						
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			progressBar1.setMaximum((int) (dureeTotale / 1000));
			FFPROBE.dureeTotale = progressBar1.getMaximum();
			FFMPEG.dureeTotale = progressBar1.getMaximum();
			
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			FFMPEG.error  = true;
			if (listeBAB.exists())
				listeBAB.delete();
		}//End Try
		
		return listeBAB;
	}

	protected static boolean analyse(File file) throws InterruptedException {
		 FFPROBE.FrameData(file.toString());	
		 do
		 	Thread.sleep(100);						 
		 while (FFPROBE.isRunning);
		 
		 if (errorAnalyse(file.toString()))
			 return false;
		 						 					 
		 FFPROBE.Data(file.toString());

		 do
			Thread.sleep(100);
		 while (FFPROBE.isRunning);
		 					 
		 if (errorAnalyse(file.toString()))
			return false;
		 
		 return true;
	}
	
	protected static String setAudioFiles(String audioFiles, File file) {
    	int channels = 1;
		for (int i2 = 0 ; i2 < liste.getSize() ; i2++)
		{
			File audioName = new File(liste.getElementAt(i2));
			audioName = new File(audioName.getName().substring(0, audioName.getName().lastIndexOf(".")));							
			File videoName = new File(file.getName().substring(0, file.getName().lastIndexOf(".")));
			
			if (audioName.toString().contains(videoName.toString()) && audioName.toString().equals(videoName.toString()) == false) //L'audio contient le nom du fichier vidéo
				{
					audioFiles += " -i " + '"' + liste.getElementAt(i2) + '"' + " ";
					channels ++;
				}
		}
		
		for (int map = 0 ; map < channels ; map++)
		{
			if (map > 0)
				audioFiles += "-map " + map + ":a ";
		}
		
		return audioFiles;	
	}

	protected static String setAudio(String audioFiles) {

		String audio = " -c:a pcm_s16le";
		if (FFPROBE.qantization == 24)
			audio = " -c:a pcm_s24le";
		else if (FFPROBE.qantization == 32)
			audio = " -c:a pcm_s32le"; 
		
        if (caseConform.isSelected() && (comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySpeed")) || comboConform.getSelectedItem().toString().equals(language.getProperty("conformByReverse"))))     
        {
        	float newFPS = Float.parseFloat((comboFPS.getSelectedItem().toString()).replace(",", "."));
        	float value = (float) (newFPS/ FFPROBE.currentFPS);
        	if (value < 0.5f || value > 2.0f)
        		return " -an";
        	else
        		audio += " -filter:a " + ("atempo=" + value);	
        }
			
        if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySlowMotion")))
        	return " -an";
        else if (caseOPATOM.isSelected())
			return audioFiles + audio + " -ar 48000";
		else
		{
			String mapping = "";
			if (comboAudio1.getSelectedIndex() == 0
				&& comboAudio2.getSelectedIndex() == 1
				&& comboAudio3.getSelectedIndex() == 2
				&& comboAudio4.getSelectedIndex() == 3
				&& comboAudio5.getSelectedIndex() == 4
				&& comboAudio6.getSelectedIndex() == 5
				&& comboAudio7.getSelectedIndex() == 6
				&& comboAudio8.getSelectedIndex() == 7)
			{
				return audio + " -ar 48000 -map a?";	
			}
			else
			{
				if (comboAudio1.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio1.getSelectedIndex()) + "?";
				if (comboAudio2.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio2.getSelectedIndex()) + "?";
				if (comboAudio3.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio3.getSelectedIndex()) + "?";
				if (comboAudio4.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio4.getSelectedIndex()) + "?";
				if (comboAudio5.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio5.getSelectedIndex()) + "?";
				if (comboAudio6.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio6.getSelectedIndex()) + "?";
				if (comboAudio7.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio7.getSelectedIndex()) + "?";
				if (comboAudio8.getSelectedIndex() != 8)
					mapping += " -map a:" + (comboAudio8.getSelectedIndex()) + "?";
			}
			
			return audio + " -ar 48000" + mapping;	
		}
	}
	
	protected static String setSequence(File file, String extension) {
		if (caseActiverSequence.isSelected())
		{
			int n = 0;
			do {
				n ++;
			} while (file.toString().substring(file.toString().lastIndexOf(".") - n).replace(extension, "").matches("[0-9]+") != false);					
			
			if (caseBlend.isSelected())
				return " -start_number " + file.toString().substring(file.toString().lastIndexOf(".") - n + 1).replace(extension, "");	
			else	
				return " -framerate " + caseSequenceFPS.getText() + " -start_number " + file.toString().substring(file.toString().lastIndexOf(".") - n + 1).replace(extension, "");	
		}
		
		return "";
	}
	
	protected static File setSequenceName(File file, String extension) {
		if (caseActiverSequence.isSelected())
		{
			int n = 0;
			do {
				n ++;
			} while (file.toString().substring(file.toString().lastIndexOf(".") - n).replace(extension, "").matches("[0-9]+") != false);	
			
			int nombre = (n - 1);
			file = new File(file.toString().substring(0, file.toString().lastIndexOf(".") - nombre) + "%0" + nombre + "d" + extension);				
		}
		return file;
	}

	protected static String setCrop(String filterComplex) {		
		
		if (caseRognerImage.isSelected())
		{
			if (filterComplex != "")
				filterComplex += "," + Shutter.cropFinal;
			else
				filterComplex = "" + Shutter.cropFinal;
		}
    	
    	return filterComplex;
	}
	
	protected static String setPad(String filterComplex) {	
		
		if (caseForcerResolution.isSelected())
		{
			String s[] = lblResolution.getSelectedItem().toString().split("x");
			
			if (lblPad.getText().equals(language.getProperty("lblPad")))
			{
				if (filterComplex != "") filterComplex += ",";
					filterComplex += "scale="+s[0]+":"+s[1]+":force_original_aspect_ratio=decrease,pad=" +s[0]+":"+s[1]+":(ow-iw)*0.5:(oh-ih)*0.5";
			}
			else if (lblPad.getText().equals(language.getProperty("lblStretch")))
			{
				if (filterComplex != "") filterComplex += ",";
					filterComplex += "scale="+s[0]+":"+s[1];				
			}
			else 
			{
				String i[] = FFPROBE.imageResolution.split("x");        	
	
	        	int iw = Integer.parseInt(i[0]);
	        	int ih = Integer.parseInt(i[1]);          	
	        	int ow = Integer.parseInt(s[0]);
	        	int oh = Integer.parseInt(s[1]);        	
	        	float ir = (float) iw / ih;
	        	
	        	if (filterComplex != "") filterComplex += ",";
	        	
	        	//Original sup. à la sortie
	        	if (iw > ow || ih > oh)
	        	{
	        		//Si la hauteur calculée est > à la hauteur de sortie
	        		if ( (float) ow / ir >= oh)
	        			filterComplex += "scale=" + ow + ":-1,crop=" + "'" + ow + ":" + oh + ":0:(ih-oh)*0.5" + "'";
	        		else
	        			filterComplex += "scale=-1:" + oh + ",crop=" + "'" + ow + ":" + oh + ":(iw-ow)*0.5:0" + "'";
	        	}
	        	else
	        		filterComplex += "scale=" + ow + ":" + oh;
			}
			
		}
		
		return filterComplex;
	}
	
	protected static String setDAR(String filterComplex) {
		if (caseForcerDAR.isSelected())
		{
			if (filterComplex != "") filterComplex += ",";
				filterComplex += "setdar=" + comboDAR.getSelectedItem().toString().replace(":", "/");
		}
    	
    	return filterComplex;
	}
	
	protected static String setLoop(String extension) {
		if (caseActiverSequence.isSelected() == false)
		{
			switch (extension)
			{
				case ".jpg":
				case ".jpeg":
				case ".png":
				case ".tif":
				case ".tiff":
				case ".tga":
				case ".bmp":
				case ".psd":
					Shutter.progressBar1.setMaximum(10);
					return " -loop 1 -t " + Settings.txtImageDuration.getText();								
			}
		}
		
		return "";
	}

	protected static String setInterlace() {		
        if (caseForcerProgressif.isSelected())
        	return " -field_order progressive" ;  
        else if (FFPROBE.entrelaced.equals("1") || caseForcerEntrelacement.isSelected())
        {               	
          if ((FFPROBE.entrelaced.equals("1") && FFPROBE.fieldOrder.equals("0")) || caseForcerInversion.isSelected()) //Invertion de trames
        	  return " -field_order bt";                    	
          else
        	  return " -field_order tt"; 
        }		   	
        
		return "";
	}
		
	protected static String setDeinterlace() {		
		if (caseForcerDesentrelacement.isSelected())
		{
			int doubler = 0;
			if (lblTFF.getText().equals("x2"))
				doubler = 1;
			
			return comboForcerDesentrelacement.getSelectedItem().toString() + "=" + doubler + ":" + FFPROBE.fieldOrder + ":0";
		}	
		else if (FFPROBE.entrelaced.equals("1") && caseConform.isSelected() && (comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySlowMotion")) || comboConform.getSelectedItem().toString().equals(language.getProperty("conformByInterpolation"))))
			return comboForcerDesentrelacement.getSelectedItem().toString() + "=0:" + FFPROBE.fieldOrder + ":0";
		else
			return "";
	}
	
	protected static String setBlend(String filterComplex) {
		if (caseBlend.isSelected())
		{			
			int value = sliderBlend.getValue();
			StringBuilder blend = new StringBuilder();
			for (int i = 0 ; i < value ; i++)
			{
				blend.append("tblend=all_mode=average,");
			}	
			
			blend.append("setpts=" + (float) 25 / Float.parseFloat(caseSequenceFPS.getText()) + "*PTS");
			
			if (filterComplex != "")
				filterComplex += "," + blend;	
			else
				filterComplex = "" + blend;	
		}
		return filterComplex;
	}
	
	protected static String setMotionBlur(String filterComplex) {
		if (caseMotionBlur.isSelected())
		{			
			float fps = Float.parseFloat(caseSequenceFPS.getText()) * 2;
			if (filterComplex != "")
				filterComplex += ",minterpolate=fps=" + fps + ",tblend=all_mode=average,framestep=2";	
			else
				filterComplex = "minterpolate=fps=" + fps + ",tblend=all_mode=average,framestep=2";	
		}
		return filterComplex;
	}
	
	protected static String setLUT(String filterComplex) {
		if (caseLUTs.isSelected())
		{			
			String pathToLuts;
			if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
			{
				pathToLuts = Shutter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
				pathToLuts = pathToLuts.substring(0,pathToLuts.length()-1);
				pathToLuts = pathToLuts.substring(0,(int) (pathToLuts.lastIndexOf("/"))).replace("%20", "\\ ")  + "/LUTs/";
			}
			else
				pathToLuts = "LUTs/";
			
			if (filterComplex != "")
				filterComplex += ",lut3d=file=" + pathToLuts + Shutter.comboLUTs.getSelectedItem().toString();	
			else
				filterComplex = "lut3d=file=" + pathToLuts + Shutter.comboLUTs.getSelectedItem().toString();	
		}
		return filterComplex;
	}
	
	protected static String setColor(String filterComplex) {
		if (caseColor.isSelected())
		{			
			if (filterComplex != "")
				filterComplex += "," + finalEQ;	
			else
				filterComplex = finalEQ;	
		}

		return filterComplex;
	}
	
	protected static String setInterlace50p(String videoFilter) {
		if (caseForcerEntrelacement.isSelected())
		{			
			if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySlowMotion")) == false)
			{
				if (caseConform.isSelected() && Float.valueOf(comboFPS.getSelectedItem().toString().replace(",", ".")) >= 50.0f)
				{
					if (videoFilter != "")
						videoFilter += ",format=yuv444p,interlace";
					else
						videoFilter = "format=yuv444p,interlace";
				}
			}
			else if (FFPROBE.currentFPS == 50.0f || FFPROBE.currentFPS == 59.94f || FFPROBE.currentFPS == 60.0f)
			{
				if (videoFilter != "")
					videoFilter += ",format=yuv444p,interlace";
				else
					videoFilter = "format=yuv444p,interlace";
			}						
		}
		return videoFilter;
	}
	
	protected static String setStabilisation(File vidstab, String filterComplex, File file, String fichier, String concat) throws InterruptedException {
		
		if (caseStabilisation.isSelected())
		{
			if (System.getProperty("os.name").contains("Windows"))
				vidstab = new File("vidstab.trf");
			else
			{
						    		
				vidstab = new File(Shutter.dirTemp + "vidstab.trf");
			}		
			
			lblEncodageEnCours.setText(Shutter.language.getProperty("analyzeOf") + " " + fichier);
			
			//Analyse du fichier
			String cmd;
			if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
				cmd =  " -an -pix_fmt yuv420p -f yuv4mpegpipe pipe:stab | PathToFFMPEG -i pipe:stab -vf vidstabdetect=result=" + vidstab.toString() + " -y -f null -";					
			else
				cmd =  " -an -pix_fmt yuv420p -f yuv4mpegpipe pipe:stab | PathToFFMPEG -i pipe:stab -vf vidstabdetect=result=" + vidstab.toString() + " -y -f null -" + '"';	
			
			FFMPEG.run(FFMPEG.inPoint + concat + " -i " + '"' + file.toString() + '"' + FFMPEG.postInPoint + FFMPEG.outPoint + cmd);		
			
			//Attente de la fin de FFMPEG
			do
				Thread.sleep(100);
			while(FFMPEG.runProcess.isAlive());						
			
			if (filterComplex != "")
				filterComplex += ",vidstabtransform=input=" + vidstab.toString();
			else
				filterComplex = "vidstabtransform=input=" + vidstab.toString();
			
			lblEncodageEnCours.setText(fichier);
		}
		return filterComplex;
	}
		
	protected static String setColorspace() {
		if (caseColorspace.isSelected())
		{
			if (comboColorspace.getSelectedItem().equals("Rec. 709"))
				return " -color_primaries bt709 -color_trc bt709 -colorspace bt709";
			else if (comboColorspace.getSelectedItem().equals("Rec. 2020 PQ"))
				return " -color_primaries bt2020 -color_trc smpte2084 -colorspace bt2020nc";
			else if (comboColorspace.getSelectedItem().equals("Rec. 2020 HLG"))
				return " -color_primaries bt2020 -color_trc arib-std-b67 -colorspace bt2020nc";
			else
				return "";
		}
		else
			return "";
	}
	
	protected static String setRotate(String filter) {
		String rotate = "";
		if (caseRotate.isSelected()) {
			String transpose = "";
			switch (comboRotate.getSelectedItem().toString()) {
			case "90":
				if (caseMiror.isSelected())
					transpose = "transpose=3";
				else
					transpose = "transpose=1";
				break;
			case "-90":
				if (caseMiror.isSelected())
					transpose = "transpose=0";
				else
					transpose = "transpose=2";
				break;
			case "180":
				if (caseMiror.isSelected())
					transpose = "transpose=1,transpose=1,hflip";
				else
					transpose = "transpose=1,transpose=1";
				break;
			}

			rotate = transpose;
		}
		else if (caseMiror.isSelected())
			rotate = "hflip";
					
		if (rotate != "")
		{
			if (filter != "")
				filter += "," + rotate;
			else	
				filter = "" + rotate;	
		}

		return filter;
	}
	
	protected static String setDeflicker(String filterComplex) {
		if (caseDeflicker.isSelected())
		{
			if (filterComplex != "")
				filterComplex += ",split[a][b];[b]setpts=PTS-STARTPTS+" + (1 / FFPROBE.currentFPS) + "/TB,format=rgba,colorchannelmixer=aa=0.5[deflicker];[a][deflicker]overlay=shortest=1";
			else
				filterComplex = "split[a][b];[b]setpts=PTS-STARTPTS+" + (1 / FFPROBE.currentFPS) + "/TB,format=rgba,colorchannelmixer=aa=0.5[deflicker];[a][deflicker]overlay=shortest=1";		
		}
		return filterComplex;
	}
	
	protected static String setDeband(String filterComplex) {
		
		if (caseBanding.isSelected())
		{
			if (filterComplex != "")
				filterComplex += ",deband=r=32";
			else
				filterComplex = "deband=r=32";
		}	
		return filterComplex;
	}
	
	protected static String setDetails(String filterComplex) {
		
		if (caseDetails.isSelected())
		{
			float value = (0 - (float) sliderDetails.getValue() / 10);
			
			if (filterComplex != "")
				filterComplex += ",smartblur=1.0:" + value;
			else
				filterComplex = "smartblur=1.0:" + value;
		}	
		return filterComplex;
	}
	
	protected static String setFlags() { 
		
		if (caseDetails.isSelected())
    	{
			float value = (0 - (float) sliderDetails.getValue() / 10);
			
			if (value > 0)
				return " -sws_flags lanczos";
    	}
		return "";
	}
	
	protected static String setDenoiser(String filterComplex) {
		if (caseBruit.isSelected())
		{
			int value = sliderBruit.getValue();
			
			if (filterComplex != "")
				filterComplex += ",hqdn3d=" + value + ":" + value + ":" + value + ":" + value;
			else
				filterComplex = "hqdn3d=" + value + ":" + value + ":" + value + ":" + value;
		}
		
		return filterComplex;
	}

	protected static String setExposure(String filterComplex) {
		if (caseExposure.isSelected())
		{
			int value = sliderExposure.getValue();
			
			if (filterComplex != "")
				filterComplex += ",deflicker=s=" + Math.ceil((128 * value) / 100 + 1);	
			else
				filterComplex = "deflicker=s=" + Math.ceil((128 * value) / 100 + 1);	
		}
		
		return filterComplex;
	}
	
	protected static String setInterpolation(String filterComplex) {
		if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformByInterpolation")))
		{		            		
			float newFPS = Float.parseFloat((comboFPS.getSelectedItem().toString()).replace(",", "."));  
				
			if (filterComplex != "") filterComplex += ",";
			
			filterComplex += "minterpolate=fps=" + newFPS;            
		}

		return filterComplex;
	}
	
	protected static String setSlowMotion(String filterComplex) {
		if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySlowMotion")))
        {		            		
        	float newFPS = Float.parseFloat((comboFPS.getSelectedItem().toString()).replace(",", "."));  
        		
            if (filterComplex != "") filterComplex += ",";
            
            filterComplex += "minterpolate=fps=" + newFPS + ",setpts=" + (newFPS / FFPROBE.currentFPS) + "*PTS";            
        }
		
		return filterComplex;
	}
	
	protected static String setPTS(String videoFilter) {
		if (caseConform.isSelected() && (comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySpeed")) || comboConform.getSelectedItem().toString().equals(language.getProperty("conformByReverse"))))
        {		            		
        	float newFPS = Float.parseFloat((comboFPS.getSelectedItem().toString()).replace(",", "."));
            
            if (videoFilter != "") videoFilter += ",";
            	
            videoFilter += "setpts=" + (FFPROBE.currentFPS / newFPS) + "*PTS";    

    		if (comboConform.getSelectedItem().toString().equals(language.getProperty("conformByReverse")))		            		
                videoFilter += ",reverse";   			
        }
		
		return videoFilter;
	}
	
	protected static String setLogo() {
		if (caseLogo.isSelected())	        	
			return " -i " + '"' + WatermarkWindow.logoFile + '"'; 
		else
			return "";

	}
	
	protected static String setWatermark(String filterComplex) {
        if (caseLogo.isSelected())
        {		        	
        	if (filterComplex != "") 	
        	{
            	filterComplex = "[0:v]" + filterComplex + "[v];[1:v]scale=iw*" + ((float)  Integer.parseInt(WatermarkWindow.textSize.getText()) / 100) + ":ih*" + ((float) Integer.parseInt(WatermarkWindow.textSize.getText()) / 100) +			
        				",lut=a=val*" + ((float) Integer.parseInt(WatermarkWindow.textOpacity.getText()) / 100) + 
        				"[scaledwatermark];[v][scaledwatermark]overlay=" + WatermarkWindow.textPosX.getText() + ":" + WatermarkWindow.textPosY.getText();
        	}
        	else
        	{
            	filterComplex = "[1:v]scale=iw*" + ((float)  Integer.parseInt(WatermarkWindow.textSize.getText()) / 100) + ":ih*" + ((float) Integer.parseInt(WatermarkWindow.textSize.getText()) / 100) +			
        				",lut=a=val*" + ((float) Integer.parseInt(WatermarkWindow.textOpacity.getText()) / 100) + 
        				"[scaledwatermark];[0:v][scaledwatermark]overlay=" + WatermarkWindow.textPosX.getText() + ":" + WatermarkWindow.textPosY.getText();
        	}
        }
        
		return filterComplex;
	}
	
	protected static String setSubtitles() {
    	if (caseSubtitles.isSelected())
    	{    		
    		String background = "" ;
			if (SubtitlesWindow.lblBackground.getText().equals(Shutter.language.getProperty("lblBackgroundOn")))
				background = ",BorderStyle=4,BackColour=&H" + SubtitlesWindow.alpha + SubtitlesWindow.hex2 + "&,Outline=0";
			else
				background = ",OutlineColour=&H" + SubtitlesWindow.alpha + SubtitlesWindow.hex2 + "&";
			
			//Bold
			if (SubtitlesWindow.btnG.getForeground() != Color.BLACK)
				background += ",Bold=1";
			
			//Italic
			if (SubtitlesWindow.btnI.getForeground() != Color.BLACK)
				background += ",Italic=1";
    		
			String i[] = FFPROBE.imageResolution.split("x");
			if (caseForcerResolution.isSelected())
			{
	   			String s[] = lblResolution.getSelectedItem().toString().split("x");
	        	int iw = Integer.parseInt(i[0]);
	        	int ih = Integer.parseInt(i[1]);
	        	int ow = Integer.parseInt(s[0]);
	        	int oh = Integer.parseInt(s[1]);      
	        		        	
	        	int width = (int) ((float) Integer.parseInt(SubtitlesWindow.textWidth.getText()) / ((float) iw/ow));	        		        	
	        	int height = (int) ((float) (ih + Integer.parseInt(SubtitlesWindow.spinnerSubtitlesPosition.getValue().toString())) / ((float) ih/oh));

				return " -f lavfi" + FFMPEG.inPoint + " -i " + '"' + "color=black@0.0,format=rgba,scale=" + width + ":" + height + ",subtitles=" + "'" + subtitlesFile.toString() + "':alpha=1:force_style='FontName=" + SubtitlesWindow.comboFont.getSelectedItem().toString() + ",FontSize=" + SubtitlesWindow.spinnerSize.getValue() + ",PrimaryColour=&H" + SubtitlesWindow.hex + "&" + background + "'" + '"';
			}
			else
				return " -f lavfi" + FFMPEG.inPoint + " -i " + '"' + "color=black@0.0,format=rgba,scale=" + SubtitlesWindow.textWidth.getText() + ":" + i[1] + "+" + SubtitlesWindow.spinnerSubtitlesPosition.getValue() + ",subtitles=" + "'" + subtitlesFile.toString() + "':alpha=1:force_style='FontName=" + SubtitlesWindow.comboFont.getSelectedItem().toString() + ",FontSize=" + SubtitlesWindow.spinnerSize.getValue() + ",PrimaryColour=&H" + SubtitlesWindow.hex + "&" + background + "'" + '"';		
			 
		}
    	
    	return "";
	}
	
	protected static String showTimecode(String filterComplex, String fichier) {
		
		 String tc1 = FFPROBE.timecode1;
		 String tc2 = FFPROBE.timecode2;
		 String tc3 = FFPROBE.timecode3;
		 String tc4 = FFPROBE.timecode4;

		if (OverlayWindow.caseAddTimecode.isSelected())
		{
			 tc1 = OverlayWindow.TC1.getText();
			 tc2 = OverlayWindow.TC2.getText();			
			 tc3 = OverlayWindow.TC3.getText();		    
			 tc4 = OverlayWindow.TC4.getText();
		}
		 
		if (caseInAndOut.isSelected() && VideoPlayer.sliderIn.getValue() > VideoPlayer.sliderIn.getMinimum() && OverlayWindow.caseAddTimecode.isSelected())
		{
			 tc1 = String.valueOf(Integer.parseInt(tc1) - Integer.parseInt(VideoPlayer.caseInH.getText()));
	         tc2 = String.valueOf(Integer.parseInt(tc2) - Integer.parseInt(VideoPlayer.caseInM.getText()));
	         tc3 = String.valueOf(Integer.parseInt(tc3) - Integer.parseInt(VideoPlayer.caseInS.getText()));
	         tc4 = String.valueOf(Integer.parseInt(tc4) - Integer.parseInt(VideoPlayer.caseInF.getText()));
		}
      
       	if (OverlayWindow.caseShowFileName.isSelected() && caseAddOverlay.isSelected())
       	{
       		if (filterComplex != "") filterComplex += ",";
       		filterComplex += "drawtext=" + OverlayWindow.font + ":text='" + fichier + "':r=" + FFPROBE.currentFPS + ":x=" + OverlayWindow.textNamePosX.getText() + ":y=" + OverlayWindow.textNamePosY.getText() + ":fontcolor=0x" + OverlayWindow.hex + OverlayWindow.hexAlphaName + ":fontsize=" + OverlayWindow.spinnerSizeName.getValue() + ":box=1:boxcolor=0x" + OverlayWindow.hex2 + OverlayWindow.hexName;
       	}
       	
       	if (OverlayWindow.caseShowText.isSelected() && caseAddOverlay.isSelected())
       	{
       		if (filterComplex != "") filterComplex += ",";
       		filterComplex += "drawtext=" + OverlayWindow.font + ":text='" + OverlayWindow.text.getText() + "':r=" + FFPROBE.currentFPS + ":x=" + OverlayWindow.textNamePosX.getText() + ":y=" + OverlayWindow.textNamePosY.getText() + ":fontcolor=0x" + OverlayWindow.hex + OverlayWindow.hexAlphaName + ":fontsize=" + OverlayWindow.spinnerSizeName.getValue() + ":box=1:boxcolor=0x" + OverlayWindow.hex2 + OverlayWindow.hexName;
       	}
       	
	   	if ((OverlayWindow.caseAddTimecode.isSelected() || OverlayWindow.caseShowTimecode.isSelected()) && caseAddOverlay.isSelected())
	   	{
	   		if (filterComplex != "") filterComplex += ",";
	   		filterComplex += "drawtext=" + OverlayWindow.font + ":timecode='" + tc1 + "\\:" + tc2 + "\\:" + tc3 + "\\:" + tc4 + "':r=" + FFPROBE.currentFPS + ":x=" + OverlayWindow.textTcPosX.getText() + ":y=" + OverlayWindow.textTcPosY.getText() + ":fontcolor=0x" + OverlayWindow.hex + OverlayWindow.hexAlphaTc + ":fontsize=" + OverlayWindow.spinnerSizeTC.getValue() + ":box=1:boxcolor=0x" + OverlayWindow.hex2 + OverlayWindow.hexTc + ":tc24hmax=1";	      
	   	}  
	   
		return filterComplex;
	}
	
	protected static String setOverlay(String filterComplex) {
    	if (caseSubtitles.isSelected())
    	{    		
        	String i[] = FFPROBE.imageResolution.split("x");
        	int ImageWidth = Integer.parseInt(i[0]);
        	
        	int posX = ((int) (ImageWidth - Integer.parseInt(SubtitlesWindow.textWidth.getText())) / 2);
        	if (caseForcerResolution.isSelected())
        	{
	        	String s[] = lblResolution.getSelectedItem().toString().split("x");
	        	int iw = Integer.parseInt(i[0]);
	        	int ow = Integer.parseInt(s[0]);  
	        	posX =  (int) (posX / ((float) iw/ow));
        	}
    		
    		if (caseLogo.isSelected())
    			filterComplex += "[p];[p][2:v]overlay=shortest=1:x=" + posX;
    		else
    		{
        		if (filterComplex != "")
        			filterComplex += "[p];[p][1:v]overlay=shortest=1:x=" + posX;
        		else
        			filterComplex = "[0:v][1:v]overlay=shortest=1:x=" + posX;
    		}
      	}
    	
    	return filterComplex;
	}
	
	protected static String setLimiter(String filterComplex) {
		if (caseLimiter.isSelected())
		{			
			if (filterComplex != "") filterComplex += ",";

			if (FFPROBE.imageDepth == 8)
				filterComplex = "limiter=16:235";
			else if (FFPROBE.imageDepth == 10)
				filterComplex = "limiter=64:940";
			else if (FFPROBE.imageDepth == 12)
				filterComplex = "limiter=256:3760";
			else if (FFPROBE.imageDepth == 16)
				filterComplex = "limiter=4096:60160";
		}	
		return filterComplex;
	}
	
	protected static String setTimecode() {
		if (caseSetTimecode.isSelected())
			return " -timecode " + TCset1.getText() + ":" + TCset2.getText() + ":" + TCset3.getText() + ":" + TCset4.getText();
		else if (FFPROBE.timecode1 != "")
			return " -timecode " + FFPROBE.timecode1 + ":" + FFPROBE.timecode2 + ":" + FFPROBE.timecode3 + ":" + FFPROBE.timecode4;
		
		return "";
	}
	
	protected static String setConform(String filterComplex) {				
		if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformByBlending")))
		{
			float newFPS = Float.valueOf(comboFPS.getSelectedItem().toString().replace(",", "."));
			
			float FPS = FFPROBE.currentFPS;
			if (caseActiverSequence.isSelected())
				FPS = Float.valueOf(caseSequenceFPS.getText().replace(",", "."));
			
			if (FPS != newFPS)
			{	            
				if (filterComplex != "") filterComplex += ",";       
				
				filterComplex += "minterpolate=fps=" + newFPS + ":mi_mode=blend";
			}
		}
		
		return filterComplex;
	}
	
	protected static String setFilterComplex(String filterComplex, String audio) {
		
		if (caseOPATOM.isSelected())
			audio = "";
		
        if (filterComplex != "")
        {	   	   
        	//Si une des cases est sélectionnée alors il y a déjà [0:v]
        	if (caseLogo.isSelected() || caseSubtitles.isSelected())
        		filterComplex = " -filter_complex " + '"' + filterComplex + "[out]";
        	else
        		filterComplex = " -filter_complex " + '"' + "[0:v]" + filterComplex + "[out]";
    		
        	filterComplex += '"' + " -map " + '"' + "[out]" + '"' +  audio;
        }
        else       	        	
        	filterComplex = " -map v" + audio;
        
        return filterComplex;
	}
	
	protected static String setOPATOM(String audio) {
		if (caseOPATOM.isSelected())
			return audio;
					
		return "";
	}
	
	protected static String setFramerate() {
		if (caseForcerEntrelacement.isSelected() && caseConform.isSelected() && comboFPS.getSelectedItem().toString().equals("50"))
			return " -r 25";
		else if (caseForcerEntrelacement.isSelected() && caseConform.isSelected() && comboFPS.getSelectedItem().toString().equals("59,94"))
			return " -r 29.97";
		else if (caseForcerEntrelacement.isSelected() && caseConform.isSelected() && comboFPS.getSelectedItem().toString().equals("60"))
			return " -r 30";
		else if (caseConform.isSelected() && comboConform.getSelectedItem().toString().equals(language.getProperty("conformBySlowMotion")))
			return " -r " + FFPROBE.currentFPS;
		else if (caseConform.isSelected())
			return " -r " + Float.valueOf(comboFPS.getSelectedItem().toString().replace(",", "."));
		else if (caseActiverSequence.isSelected())
		{
			if (caseConform.isSelected())
				return " -r " + Float.valueOf(comboFPS.getSelectedItem().toString().replace(",", ".")) + " -frames:v " + liste.getSize();	
			else
				return " -r " + caseSequenceFPS.getText() + " -frames:v " + liste.getSize();
		}
		
		return "";
	}

	protected static String setSortie(String sortie, File file) {					
		if (caseChangeFolder1.isSelected())
		{
			sortie = lblDestination1.getText();
			
			if (caseCreateTree.isSelected())
			{ 	
				File pathToFile = null;
				if (System.getProperty("os.name").contains("Mac") || System.getProperty("os.name").contains("Linux"))
				{
					String s[] = file.getParent().toString().split("/");
					pathToFile = new File(lblDestination1.getText() + file.getParent().toString().replace("/Volumes", "").replace(s[2], ""));	
				}
				else
					pathToFile = new File (lblDestination1.getText() + file.getParent().toString().substring(2));
				
				if (pathToFile.exists() == false)
					pathToFile.mkdirs();
				
				sortie = pathToFile.toString();
			}
		}
		else
		{
			sortie =  file.getParent();
			lblDestination1.setText(sortie);
		}
		
		return sortie;
	}
	
	public static String getRandomHexString(){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < 10){
            sb.append(Integer.toHexString(r.nextInt()));
        }

        return sb.toString().substring(0, 10);
    }
	
	private static boolean errorAnalyse (String fichier)
	{
		 if (FFMPEG.error)
		 {
				FFMPEG.errorList.append(fichier);
			    FFMPEG.errorList.append(System.lineSeparator());
				return true;
		 }
		 return false;
	}

	private static boolean actionsDeFin(String fichier, File fileOut, String sortie, File vidstab) {
		
		//Erreurs
		if (FFMPEG.error || fileOut.length() == 0 || caseCreateOPATOM.isSelected())
		{
			if (FFMPEG.error)
			{
				FFMPEG.errorList.append(fichier);
				FFMPEG.errorList.append(System.lineSeparator());
		    }
			try {
				fileOut.delete();
			} catch (Exception e) {}
		}

		//Stabilisation
		if (vidstab != null)
		{
			if (vidstab.exists())
				vidstab.delete();
		}
		
		//Traitement de la file en Bout à bout
		if (Settings.btnSetBab.isSelected())
		{		
			final String extension =  fichier.substring(fichier.lastIndexOf("."));
			File listeBAB = new File(sortie.replace("\\", "/") + "/" + fichier.replace(extension, ".txt")); 			
			listeBAB.delete();
		}
		
		//Annulation
		if (cancelled)
		{
			try {
				fileOut.delete();
			} catch (Exception e) {}
			return true;
		}
		
		//Fichiers terminés
		if (cancelled == false && FFMPEG.error == false)
		{
			complete++;
			lblTermine.setText(Utils.fichiersTermines(complete));
		}
		
		//Ouverture du dossier
		if (caseOpenFolderAtEnd1.isSelected() && cancelled == false && FFMPEG.error == false)
		{
			try {
				Desktop.getDesktop().open(new File(sortie));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Envoi par e-mail et FTP
		Utils.sendMail(fichier);
		Wetransfer.addFile(fileOut);
		Ftp.sendToFtp(fileOut);
		Utils.copyFile(fileOut);
		
		//Séquence d'images et bout à bout
		if (caseActiverSequence.isSelected() || Settings.btnSetBab.isSelected())
			return true;
		
		//Timecode
		if (caseIncrementTimecode.isSelected())
		{			 
			NumberFormat formatter = new DecimalFormat("00");

			int timecodeToMs = Integer.parseInt(TCset1.getText()) * 3600000 + Integer.parseInt(TCset2.getText()) * 60000 + Integer.parseInt(TCset3.getText()) * 1000 + Integer.parseInt(TCset4.getText()) * (int) (1000 / FFPROBE.currentFPS);
			int millisecondsToTc = timecodeToMs + FFPROBE.dureeTotale;
			
			if (caseInAndOut.isSelected())
				millisecondsToTc = timecodeToMs + VideoPlayer.dureeHeures * 3600000 + VideoPlayer.dureeMinutes * 60000 + VideoPlayer.dureeSecondes * 1000 + VideoPlayer.dureeImages * (int) (1000 / FFPROBE.currentFPS);
			
			TCset1.setText(formatter.format(millisecondsToTc / 3600000));
			TCset2.setText(formatter.format((millisecondsToTc / 60000) % 60));
			TCset3.setText(formatter.format((millisecondsToTc / 1000) % 60));				        
			TCset4.setText(formatter.format((int) (millisecondsToTc / (1000 / FFPROBE.currentFPS) % FFPROBE.currentFPS)));	
		}
		
		//Scan
		if (Shutter.scanIsRunning)
		{
			Utils.moveScannedFiles(fichier);
			QTAnimation.main();
			return true;
		}
		return false;
	}
	
}//Class
