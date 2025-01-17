package net.sourceforge.squirrel_sql.plugins.graph;

import net.sourceforge.squirrel_sql.client.session.ISession;
import net.sourceforge.squirrel_sql.plugins.graph.nondbconst.DndCallback;
import net.sourceforge.squirrel_sql.plugins.graph.xmlbeans.TableFrameXmlBean;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.plaf.basic.BasicInternalFrameUI;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;


public class TableFrame extends JInternalFrame
{
   GraphTextAreaFactory txtColumsFactory;
   JScrollPane scrollPane;
   private MyUI _myUI;
   private GraphPlugin _plugin;
   private ModeManager _modeManager;
   private SortColumnsListener _sortColumnsListener;
   private ModeManagerListener _modeManagerListener;
   private ZoomerListener _zoomerListener;


   public TableFrame(ISession session, GraphPlugin plugin, String tableName, TableFrameXmlBean xmlBean, TableToolTipProvider toolTipProvider, ModeManager modeManager, DndCallback dndCallback, SortColumnsListener sortColumnsListener)
   {
      _plugin = plugin;
      _modeManager = modeManager;
      _sortColumnsListener = sortColumnsListener;

      scrollPane = new JScrollPane();
      scrollPane.setBorder(null);

      getContentPane().add(scrollPane);

      setMaximizable(false);
      setTitleBarIconsVisible(true);

      setTitle(tableName);
      GraphColoring.setTableFrameBackground(this);

      setResizable(true);


      setFrameIcon(null);

      _myUI = new MyUI(this);
      setUI(_myUI);

      txtColumsFactory = new GraphTextAreaFactory(tableName, session, plugin, toolTipProvider, modeManager, dndCallback);
      scrollPane.setViewportView(txtColumsFactory.getComponent(modeManager.getMode()));
      
      if(null != xmlBean)
      {
         double zoom = _modeManager.getZoomer().getZoom();

         Rectangle r = new Rectangle();
         r.x = (int)(zoom*xmlBean.getX() + 0.5);
         r.y = (int)(zoom*xmlBean.getY() + 0.5);
         r.width = (int)(zoom*xmlBean.getWidht() + 0.5);
         r.height = (int)(zoom*xmlBean.getHeight() + 0.5);
         setBounds(r);

         setTitleBarIconsVisible(Mode.ZOOM_PRINT != _modeManager.getMode());
      }

      _zoomerListener = new ZoomerListener()
      {
         public void zoomChanged(double newZoom, double oldZoom, boolean adjusting)
         {
         }


         public void setHideScrollBars(boolean b)
         {
         }
      };

      _modeManagerListener = new ModeManagerListener()
      {
         @Override
         public void modeChanged(Mode newMode)
         {
            onModeChanged(newMode);
         }
      };
      
      setBorder(new LineBorder(Color.BLACK));
   }

   private void setTitleBarIconsVisible(boolean visible)
   {
      setClosable(visible);

      // Iconify is abused for sort
      setIconifiable(visible);
   }

   public void setVisible(boolean b)
   {
      if (null != _modeManager)
      {
         if (b)
         {
            _modeManager.getZoomer().addZoomListener(_zoomerListener);
            _modeManager.addModeManagerListener(_modeManagerListener);
            onModeChanged(_modeManager.getMode());
         }
         else
         {
            _modeManager.getZoomer().removeZoomListener(_zoomerListener);
            _modeManager.removeModeManagerListener(_modeManagerListener);
         }
      }

      super.setVisible(b);
   }




   private void onModeChanged(Mode newMode)
   {
      scrollPane.setViewportView(txtColumsFactory.getComponent(_modeManager.getMode()));
      setTitleBarIconsVisible(Mode.ZOOM_PRINT != newMode);
   }

   public TableFrame.MyTitlePaneUI getTitlePane()
   {
      return _myUI.getTitlePane();
   }

   public TableFrameXmlBean getXmlBean()
   {
      TableFrameXmlBean ret = new TableFrameXmlBean();

      double zoom = _modeManager.getZoomer().getZoom();

      Rectangle bounds = getBounds();
      ret.setX((int)(bounds.x/zoom + 0.5));
      ret.setY((int)(bounds.y/zoom + 0.5));
      ret.setWidht((int)(bounds.width/zoom + 0.5));
      ret.setHeight((int)(bounds.height/zoom + 0.5));

      return ret;

   }
   
   class MyUI extends BasicInternalFrameUI
   {
	   public MyUI(JInternalFrame frame)
      {
         super(frame);
      }


      protected JComponent createNorthPane(JInternalFrame w)
      {
         titlePane = new MyTitlePaneUI(w);
         return titlePane;
      }

      public TableFrame.MyTitlePaneUI getTitlePane()
      {
         return (MyTitlePaneUI) _myUI.titlePane;
      }


   }

   class MyTitlePaneUI extends BasicInternalFrameTitlePane
   {
      public static final int UNZOOMED_PREF_HEIGHT = 18;
      private Color groupTitleColor;


      public MyTitlePaneUI(JInternalFrame f)
      {
         super(f);
         /////////////////////////////////////////////////////////
         // Tablegroups
         this.addMouseListener(new MouseAdapter()
         {
            @Override
            public void mouseClicked(MouseEvent e)
            {
               onMouseClickedTitlePane(e);
            }
         });
         /////////////////////////////////////////////////////////


      }

      private void onMouseClickedTitlePane(MouseEvent e)
      {
         if (e.getButton() == MouseEvent.BUTTON1)
         {
            GraphDesktopPane desktopPane = (GraphDesktopPane) TableFrame.this.getDesktopPane();
            if (e.isControlDown())
            {
               if (desktopPane.isGroupFrame(TableFrame.this))
               {
                  desktopPane.removeGroupFrame(TableFrame.this);
               }
               else
               {
                  desktopPane.addGroupFrame(TableFrame.this);
               }
            }
            else
            {
               desktopPane.setGroupFrame(TableFrame.this);
            }
         }
      }


      protected void installDefaults()
      {
         super.installDefaults();


         GraphPluginResources rsc = new GraphPluginResources(_plugin);

         closeIcon = rsc.getIcon(GraphPluginResources.IKeys.TABLE_FRAME_CLOSE);

         // Iconify is abused for sort
         iconIcon = rsc.getIcon(GraphPluginResources.IKeys.SORT_NONE);

         groupTitleColor = GraphColoring.getGroupTitleColor();
         selectedTitleColor = GraphColoring.getInternalFrameTitleSelectedColor(selectedTitleColor);
         notSelectedTitleColor = GraphColoring.getInternalFrameTitleNotSelectedTitleColor(notSelectedTitleColor);

         selectedTextColor = GraphColoring.getInternalFrameTitleSelectedTextColor(selectedTextColor);
         notSelectedTextColor = GraphColoring.getInternalFrameTitleNotSelectedTextColor(notSelectedTextColor);

         setFont(new Font(getFont().getFontName(), Font.BOLD, getFont().getSize()));
      }

      @Override
      protected void createActions()
      {
         super.createActions();

         // Iconify is abused for sort
         iconifyAction = new AbstractAction()
         {
            @Override
            public void actionPerformed(ActionEvent e)
            {
               _sortColumnsListener.sortButtonClicked(iconButton);
            }
         };
      }

      protected void paintTitleBackground(Graphics g)
      {
         if (((GraphDesktopPane) frame.getDesktopPane()).isGroupFrame(TableFrame.this))
         {
            g.setColor(groupTitleColor);
         }
         else
         {
            g.setColor(notSelectedTitleColor);
         }
         g.fillRect(0, 0, getWidth(), getHeight());
      }

      public void paintComponent(Graphics g)
      {
         paintTitleBackground(g);

         if (frame.getTitle() != null)
         {
            boolean isSelected = frame.isSelected();
            Font f = g.getFont();
            g.setFont(getFont());
            if (isSelected)
               g.setColor(selectedTextColor);
            else
               g.setColor(notSelectedTextColor);

            // Center text vertically.
            FontMetrics fm = g.getFontMetrics();

            double s = _modeManager.getZoomer().getZoom();
            int baseline = ((int)(getHeight()/s) + fm.getAscent() - fm.getLeading() - fm.getDescent()) / 2;

            int titleX;
            Rectangle r = new Rectangle(0, 0, 0, 0);
            if (frame.isIconifiable())
               r = iconButton.getBounds();
            else if (frame.isMaximizable())
               r = maxButton.getBounds();
            else if (frame.isClosable()) r = closeButton.getBounds();
            int titleW;

            String title = frame.getTitle();

            if (r.x == 0) r.x = frame.getWidth() - frame.getInsets().right;
            titleX = menuBar.getX() + menuBar.getWidth() + 2;
            titleW = (int)(  (r.x - titleX - 3)/ _modeManager.getZoomer().getZoom() + 0.5  );
            title = getTitle(frame.getTitle(), fm, titleW);

            Graphics2D g2d = (Graphics2D) g;
            AffineTransform origTrans = g2d.getTransform();

            AffineTransform at = new AffineTransform(origTrans);
            at.scale(_modeManager.getZoomer().getZoom(), _modeManager.getZoomer().getZoom());
            g2d.setTransform(at);

            g.drawString(title, titleX, baseline);

            g2d.setTransform(origTrans);

            g.setFont(f);
         }
      }

      protected LayoutManager createLayout()
      {
         return new MyTitlePaneLayout();
      }


      public Dimension getPreferredSize()
      {
         Dimension ret = super.getPreferredSize();


         ret.height = (int) (UNZOOMED_PREF_HEIGHT * _modeManager.getZoomer().getZoom() + 0.5);
         return ret;
      }



      /**
       * This removes the system menu
       * @return
       */
      protected JMenuBar createSystemMenuBar()
      {
         menuBar = new JMenuBar()
         {
            public void setSize(int width, int height)
            {
               super.setSize(0,0);
            }

            public void setBounds(int x, int y, int width, int height)
            {
               super.setBounds(0, 0, 0, 0);
            }
         };
         menuBar.setBorderPainted(false);
         menuBar.setSize(0,0);
         menuBar.setBounds(0,0,0,0);
         return menuBar;
      }

      class MyTitlePaneLayout extends BasicInternalFrameTitlePane.TitlePaneLayout
      {

         public Dimension minimumLayoutSize(Container c)
         {
            Dimension ret = super.minimumLayoutSize(c);
            ret.width *= _modeManager.getZoomer().getZoom();
            ret.height *= _modeManager.getZoomer().getZoom();
            return ret;
         }
      }
      
      

   }
}
