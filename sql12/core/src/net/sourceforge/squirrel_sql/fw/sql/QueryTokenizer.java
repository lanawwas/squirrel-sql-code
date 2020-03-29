package net.sourceforge.squirrel_sql.fw.sql;
/*
 * Copyright (C) 2001-2003 Johan Compagner
 * jcompagner@j-com.nl
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.squirrel_sql.fw.preferences.IQueryTokenizerPreferenceBean;
import net.sourceforge.squirrel_sql.fw.sql.commentandliteral.NextPositionAction;
import net.sourceforge.squirrel_sql.fw.sql.commentandliteral.NextPositionResult;
import net.sourceforge.squirrel_sql.fw.sql.commentandliteral.SQLCommentAndLiteralHandler;
import net.sourceforge.squirrel_sql.fw.util.StringUtilities;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;

public class QueryTokenizer implements IQueryTokenizer
{
	protected ArrayList<QueryHolder> _queries = new ArrayList<>();
    
	protected Iterator<QueryHolder> _queryIterator;

   protected String _querySep = null;

   protected String _lineCommentBegin = null;

   protected boolean _removeMultiLineComment = true;

   protected ITokenizerFactory _tokenizerFactory = null;

   /**
    * Logger for this class.
    */
   private final static ILogger s_log = LoggerController.createLogger(QueryTokenizer.class);

   public QueryTokenizer()
   {
   }

   public QueryTokenizer(String querySep,
                         String lineCommentBegin,
                         boolean removeMultiLineComment)
   {
      _querySep = querySep;
      _lineCommentBegin = lineCommentBegin;
      _removeMultiLineComment = removeMultiLineComment;
      setFactory();
   }

   public QueryTokenizer(IQueryTokenizerPreferenceBean prefs)
   {
      this(prefs.getStatementSeparator(), prefs.getLineComment(), prefs.isRemoveMultiLineComments());
   }

   /**
    * Sets the ITokenizerFactory which is used to create additional instances
    * of the IQueryTokenizer - this is used for handling file includes
    * recursively.
    */
   protected void setFactory()
   {
      _tokenizerFactory = () -> new QueryTokenizer();
   }
    

	private int getLenOfQuerySepIfAtLastCharOfQuerySep(String sql, int i, String querySep, boolean inLiteral)
	{
		if(inLiteral)
		{
			return -1;
		}

		char c = sql.charAt(i);

		if(1 == querySep.length() && c == querySep.charAt(0))
		{
			return 1;
		}
		else
		{
			int fromIndex = i - querySep.length();
			if(0 > fromIndex)
			{
				return -1;
			}

			int querySepIndex = sql.indexOf(querySep, fromIndex);

			if(0 > querySepIndex)
			{
				return -1;
			}

			if(Character.isWhitespace(c))
			{
				if(querySepIndex + querySep.length() == i)
				{
					if(0 == querySepIndex)
					{
						return querySep.length() + 1;
					}
					else if(Character.isWhitespace(sql.charAt(querySepIndex - 1)))
					{
						return querySep.length() + 2;
					}
				}
			}
			else if(sql.length() -1 == i)
			{
				if(querySepIndex + querySep.length() - 1 == i)
				{
					if(0 == querySepIndex)
					{
						return querySep.length();
					}
					else if(Character.isWhitespace(sql.charAt(querySepIndex - 1)))
					{
						return querySep.length() + 1;
					}
				}
			}

			return -1;
		}
	}
    
	public boolean hasQuery()
	{
		return _queryIterator.hasNext();
	}

	public QueryHolder nextQuery()
	{
		return _queryIterator.next();
	}

    public void setScriptToTokenize(String script)
    {
        _queries.clear();
        
//        String MULTI_LINE_COMMENT_END = "*/";
//        String MULTI_LINE_COMMENT_BEGIN = "/*";

        script = script.replace('\r', ' ');

        StringBuffer curQuery = new StringBuffer();
        StringBuffer curOriginalQuery = new StringBuffer();

//        boolean isInLiteral = false;
//        boolean isInMultiLineComment = false;
//        boolean isInLineComment = false;
//        int literalSepCount = 0;


       SQLCommentAndLiteralHandler commentAndLiteralHandler = new SQLCommentAndLiteralHandler(script, _lineCommentBegin, _removeMultiLineComment);

        for (int i = 0; i < script.length();)
        {
           final NextPositionResult nextPositionResult = commentAndLiteralHandler.nextPosition(i);

           curOriginalQuery.append(script.charAt(i));

           if(NextPositionAction.APPEND == nextPositionResult.getNextPositionAction())
           {
              curQuery.append(script.charAt(i));
           }
           else
           {
              i = nextPositionResult.getNextPosition();
              continue;
           }


//           char c = _script.charAt(posInScript);
//
//           if(false == isInLiteral)
//           {
//                ///////////////////////////////////////////////////////////
//                // Handling of comments
//
//                // We look backwards
//                if(isInLineComment && script.startsWith("\n", i - "\n".length()))
//                {
//                    isInLineComment = false;
//                }
//
//                // We look backwards
//                if(isInMultiLineComment && script.startsWith(MULTI_LINE_COMMENT_END, i - MULTI_LINE_COMMENT_END.length()))
//                {
//                    isInMultiLineComment = false;
//                }
//
//
//                if(false == isInLineComment && false == isInMultiLineComment)
//                {
//                    // We look forward
//                    isInMultiLineComment = script.startsWith(MULTI_LINE_COMMENT_BEGIN, i);
//                    isInLineComment = script.startsWith(_lineCommentBegin, i);
//
//                    if(isInMultiLineComment && _removeMultiLineComment)
//                    {
//                        // skip ahead so the cursor is now immediately after the begin comment string
//                        i+=MULTI_LINE_COMMENT_BEGIN.length()+1;
//                    }
//                }
//
//                if((isInMultiLineComment && _removeMultiLineComment) || isInLineComment)
//                {
//                    // This is responsible that comments are not in curQuery
//                    curOriginalQuery.append(c);
//                    continue;
//                }
//                //
//                ////////////////////////////////////////////////////////////
//            }
//
//            curQuery.append(c);
//            curOriginalQuery.append(c);
//
//            if ('\'' == c)
//            {
//                if(false == isInLiteral)
//                {
//                    isInLiteral = true;
//                }
//                else
//                {
//                    ++literalSepCount;
//                }
//            }
//            else
//            {
//                if(0 != literalSepCount % 2)
//                {
//                    isInLiteral = false;
//                }
//                literalSepCount = 0;
//            }


            int querySepLen = getLenOfQuerySepIfAtLastCharOfQuerySep(script, i, _querySep, commentAndLiteralHandler.isInLiteral());

            if(-1 < querySepLen && !commentAndLiteralHandler.isInMultiLineComment())
            {
                int newLength = curQuery.length() - querySepLen;
                if(-1 < newLength && curQuery.length() > newLength)
                {
                    curQuery.setLength(newLength);

                    String newQuery = curQuery.toString().trim();
                    if(0 < newQuery.length())
                    {
                        _queries.add(new QueryHolder(curQuery.toString().trim(), curOriginalQuery.toString().trim()));
                    }
                }
                curQuery.setLength(0);
                curOriginalQuery.setLength(0);
            }

           i = nextPositionResult.getNextPosition();
        }

        String lastQuery = curQuery.toString().trim();
        String lastOriginalQuery = curOriginalQuery.toString().trim();
        if(0 < lastQuery.length())
        {
            _queries.add(new QueryHolder(lastQuery, lastOriginalQuery));
        }

        _queryIterator = _queries.iterator();
    }

    /**
     * Returns the number of queries that the tokenizer found in the script 
     * given in the last call to setScriptToTokenize, or 0 if 
     * setScriptToTokenize has not yet been called.
     */
    public int getQueryCount()
    {
       if (_queries == null)
       {
          return 0;
       }
       return _queries.size();
    }
    
    
    
    public static void main(String[] args)
    {
        //String sql = "A'''' sss ;  GO ;; GO'";
        //String sql = "A\n--x\n--y\n/*\nB";
        //String sql = "GO GO";
        String sql = "@c:\\tools\\sql\\file.sql";
        
        
        QueryTokenizer qt = new QueryTokenizer("GO", "--", true);

        qt.setScriptToTokenize(sql);
        
        while(qt.hasQuery())
        {
            System.out.println(">" + qt.nextQuery() + "<");
        }
    }

   /**
    * @return the query statement separator
    */
   public String getQuerySep()
   {
      return _querySep;
   }

   /**
    * @param sep the value to use for the query statement separator
    */
   public void setQuerySep(String sep)
   {
      _querySep = sep;
   }

   /**
    * @return the _lineCommentBegin
    */
   public String getLineCommentBegin()
   {
      return _lineCommentBegin;
   }

   /**
    * @param commentBegin the _lineCommentBegin to set
    */
   public void setLineCommentBegin(String commentBegin)
   {
      _lineCommentBegin = commentBegin;
   }

   /**
    * @return the _removeMultiLineComment
    */
   public boolean isRemoveMultiLineComment()
   {
      return _removeMultiLineComment;
   }

   @Override
   public TokenizerSessPropsInteractions getTokenizerSessPropsInteractions()
   {
      return new TokenizerSessPropsInteractions();
   }

   /**
     * @param multiLineComment the _removeMultiLineComment to set
     */
    public void setRemoveMultiLineComment(boolean multiLineComment) {
        _removeMultiLineComment = multiLineComment;
    }
    
    /** 
     * This uses statements that begin with scriptIncludePrefix to indicate 
     * that the following text is a filename containing SQL statements that 
     * should be loaded.   
     * 
     * @param scriptIncludePrefix the 
     * @param lineCommentBegin
     * @param removeMultiLineComment
     */
    protected void expandFileIncludes(String scriptIncludePrefix)
    {
       if (scriptIncludePrefix == null)
       {
          s_log.error("scriptIncludePrefix cannot be null ");
          return;
       }
       ArrayList<QueryHolder> tmp = new ArrayList<>();
       for (Iterator<QueryHolder> iter = _queries.iterator(); iter.hasNext(); )
       {
          QueryHolder sql = iter.next();
          if (sql.getQuery().startsWith(scriptIncludePrefix))
          {
             try
             {
                String filename = sql.getQuery().substring(scriptIncludePrefix.length());
                List<String> fileSQLs = getStatementsFromIncludeFile(filename);

                for (String fileSQL : fileSQLs)
                {
                   tmp.add(new QueryHolder(fileSQL));
                }

             }
             catch (Exception e)
             {
                s_log.error("Unexpected error while attempting to include file from " + sql, e);
             }

          }
          else
          {
             tmp.add(sql);
          }
       }
       _queries = tmp;
    }

   private List<String> getStatementsFromIncludeFile(String filename)
   {
      if (filename.startsWith("'"))
      {
         filename = filename.substring(1);
      }
      if (filename.endsWith("'"))
      {
         filename = StringUtilities.chop(filename);
      }
      if (filename.endsWith("\n"))
      {
         filename = StringUtilities.chop(filename);
      }
      ArrayList<String> result = new ArrayList<String>();
      if (s_log.isDebugEnabled())
      {
         s_log.debug("Attemping to open file '" + filename + "'");
      }
      File f = new File(filename);
        /*
        if (f.canRead()) {
        */
      StringBuffer fileLines = new StringBuffer();
      try
      {
         BufferedReader reader = new BufferedReader(new FileReader(f));
         String next = reader.readLine();
         while (next != null)
         {
            fileLines.append(next);
            fileLines.append("\n");
            next = reader.readLine();
         }
      }
      catch (Exception e)
      {
         s_log.error("Unexpected exception while reading lines from file (" + filename + ")", e);
      }
      if (fileLines.toString().length() > 0)
      {
         IQueryTokenizer qt = null;
         if (_tokenizerFactory != null)
         {
            qt = _tokenizerFactory.getTokenizer();
         }
         else
         {
            qt = new QueryTokenizer(_querySep, _lineCommentBegin,_removeMultiLineComment);
         }
         qt.setScriptToTokenize(fileLines.toString());
         while (qt.hasQuery())
         {
            String sql = qt.nextQuery().getQuery();
            result.add(sql);
         }
      }

      return result;
   }

    /* (non-Javadoc)
     * @see net.sourceforge.squirrel_sql.fw.sql.IQueryTokenizer#getSQLStatementSeparator()
     */
    public String getSQLStatementSeparator() {
        return _querySep;
    }

}
