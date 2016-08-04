

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


package simsql.compiler; // package mcdb.compiler;


import java.io.*;
import java.util.ArrayList;

public class FileOperation {
    
	
    public static boolean save_to_file(String path, String filename, String content){
    	File file;
    	FileWriter	fileWriter=null;
    	try{
    		file = new File(path);
    		if(!file.exists()){
    			//System.out.print("file doesn't exist!");
    			file.mkdirs();
    		}
    		file = new File(file, filename);
    		fileWriter=new FileWriter(file,false);
    		fileWriter.write(content);
    		fileWriter.close();
    		return true;
           } 
    	catch (IOException e){
    		e.printStackTrace();
    		return false;
    		}
    	
    }
    
    public static boolean is_in_file(String filename,String target){
    	String infomation=getFile_info(filename);
    	if(infomation==null)return false;
    	if(infomation.indexOf(target)>=0)return true;
    	else return false;
    }
    
    
    public static String getFile_info(String filename){
    	String result = "";
    	String temp = null;
    	try{
    		FileReader fileReader=new FileReader(filename);
    		BufferedReader  bufferedReader=new BufferedReader(fileReader);
    	    temp=bufferedReader.readLine();
    	    while(temp!=null){
    	    	result += temp;
    	    	result += "\n";
    	    	temp=bufferedReader.readLine();
    	       }
    	    fileReader.close();
    	    bufferedReader.close();
    		}
    	catch(IOException e){
    		e.printStackTrace();
			System.exit(1);
    	}
    	return result;
    }
    
    public static String[] getFile_content(String dirname)
    {
    	String temp=null;
    	ArrayList<String> resultList = new ArrayList<String>();
    	try
    	{
    		FileReader fileReader=new FileReader(dirname);
    		BufferedReader  bufferedReader=new BufferedReader(fileReader);
    	    temp=bufferedReader.readLine();
    	    while(temp!=null)
    	    {
    	    	resultList.add(temp);
    	    	temp=bufferedReader.readLine();
    	    }
    	    fileReader.close();
    	    bufferedReader.close();
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    	
    	String []strings = new String[resultList.size()];
    	resultList.toArray(strings);
    	
    	return strings;
    }
    
    public static ArrayList<String> getFile_content(String folder, String dirname)
    {
    	String temp=null;
    	ArrayList<String> resultList = new ArrayList<String>();
    	try
    	{
    		File file = new File(folder, dirname);
    		FileReader fileReader=new FileReader(file);
    		BufferedReader  bufferedReader=new BufferedReader(fileReader);
    	    temp=bufferedReader.readLine();
    	    while(temp!=null)
    	    {
    	    	resultList.add(temp);
    	    	temp=bufferedReader.readLine();
    	    }
    	    fileReader.close();
    	    bufferedReader.close();
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    	
    	return resultList;
    }
    
    public static String[] getDir(String dirname){
    	String[] dirlist;
    	try{
	    	File file=new File(dirname);
	    	dirlist=file.list();
	    	return dirlist;
    	}
    	catch(Exception e){
    		e.printStackTrace();
    		return null;
    	}
    }
   
}
