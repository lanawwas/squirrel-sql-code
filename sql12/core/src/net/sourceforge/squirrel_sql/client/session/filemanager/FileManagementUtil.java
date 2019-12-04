package net.sourceforge.squirrel_sql.client.session.filemanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileManagementUtil
{
   public static StringBuffer readFile(File file) throws IOException
   {
      StringBuffer sb = new StringBuffer();

      try(FileInputStream fis = new FileInputStream(file);
          BufferedInputStream bis = new BufferedInputStream(fis);)
      {
         byte[] bytes = new byte[2048];
         int iRead = bis.read(bytes);
         while (iRead != -1)
         {
            sb.append(new String(bytes, 0, iRead));
            iRead = bis.read(bytes);
         }
      }
      return sb;
   }
}