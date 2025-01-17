package net.sourceforge.squirrel_sql.client.mainframe.action.findprefs;

import net.sourceforge.squirrel_sql.client.Main;
import net.sourceforge.squirrel_sql.fw.gui.ComponentIndicator;
import net.sourceforge.squirrel_sql.fw.gui.GUIUtils;
import net.sourceforge.squirrel_sql.fw.util.StringManager;
import net.sourceforge.squirrel_sql.fw.util.StringManagerFactory;
import net.sourceforge.squirrel_sql.fw.util.log.ILogger;
import net.sourceforge.squirrel_sql.fw.util.log.LoggerController;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.util.List;

public class GotoHandler
{
   private StringManager s_stringMgr = StringManagerFactory.getStringManager(GotoHandler.class);

   private final static ILogger s_log = LoggerController.createLogger(GotoHandler.class);

   private PrefsFindInfo _prefsFindInfo;


   private ComponentIndicator _componentIndicator = new ComponentIndicator();

   public GotoHandler()
   {
   }

   public boolean gotoPath(List<String> path)
   {
      _prefsFindInfo = ComponentInfoByPathUtil.createPrefsFindInfo();

      PrefComponentInfo tabComponentInfo = _prefsFindInfo.showTabOfPathComponent(path);

      _prefsFindInfo = _prefsFindInfo.getPrefsFindInfoUpdate();

      if(null == tabComponentInfo)
      {
         // Happens when the dialog node was gone to.
         return true;
      }

      PrefComponentInfo componentInfoToGoTo = _prefsFindInfo.getComponentInfoByPath(path);

      if(null == componentInfoToGoTo)
      {
         s_log.warn("Failed to find Component for path (search dialog was reloaded, try again):\n" + path);

         final String pathNoNewLines = StringUtils.replace("" + path, "\n", " ");
         String msg = s_stringMgr.getString("GotoHandler.component.not.found.try.again", pathNoNewLines);
         Main.getApplication().getMessageHandler().showWarningMessage(msg);

         return false;
      }

      return blinkComponent(tabComponentInfo.getComponent(), componentInfoToGoTo.getComponent());
   }

   private boolean blinkComponent(final Component containingTabComponent, Component componentToBlink)
   {
      Runnable runnableToScrollAndBlink = null;

      if(containingTabComponent instanceof JScrollPane && containingTabComponent != componentToBlink)
      {
         final JComponent componentInScrollPane = (JComponent) ((JScrollPane) containingTabComponent).getViewport().getView();

         int x = componentToBlink.getX();
         int y = componentToBlink.getY();

         Container parent = componentToBlink.getParent();
         while (parent != componentInScrollPane)
         {
            x += parent.getX();
            y += parent.getY();
            parent = parent.getParent();
         }

         final Rectangle rect = new Rectangle(x, y, componentToBlink.getWidth(), componentToBlink.getHeight());

         runnableToScrollAndBlink = new Runnable()
         {
            public void run()
            {
               GUIUtils.forceProperty(() -> {
                  componentInScrollPane.scrollRectToVisible(rect);
                  final boolean contains = ((JScrollPane) containingTabComponent).getVisibleRect().contains(rect);
                  return contains;
               },
               () -> initBlinkComponent(componentToBlink));
            }
         };
      }
      else
      {
         runnableToScrollAndBlink = () -> initBlinkComponent(componentToBlink);
      }

      // Give the UI 300 millis to properly open
      GUIUtils.executeDelayed(runnableToScrollAndBlink, 400);

      return true;
   }

   private void initBlinkComponent(Component componentToIndicate)
   {
      _componentIndicator.init(componentToIndicate);
   }


   public PrefsFindInfo getPrefsFindInfoUpdate()
   {
      if(null == _prefsFindInfo)
      {
         throw new IllegalStateException("Call gotoPath() first");
      }

      return _prefsFindInfo;
   }
}
