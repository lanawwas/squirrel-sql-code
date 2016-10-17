package org.squirrelsql.session.graph;

import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.squirrelsql.AppState;
import org.squirrelsql.Props;
import org.squirrelsql.services.I18n;

public class AggregateFunctionPane extends BorderPane
{
   private final ImageView _imageEnabled = new ImageView(new Props(getClass()).getImage("aggfct.png"));
   private final ImageView _imageDisabled = new ImageView(new Props(getClass()).getImage("aggfct_disabled.png"));
   private boolean _enabled;

   private I18n _i18n = new I18n(getClass());

   public AggregateFunctionPane(boolean enabled)
   {
      _enabled = enabled;
      updateGraphics();

      addEventHandler(MouseEvent.MOUSE_PRESSED, e -> showPopup());
   }

   private void showPopup()
   {
      if(false == _enabled)
      {
         return;
      }


      MenuItem none = new MenuItem(_i18n.t("agg.function.none"), new ImageView(new Props(getClass()).getImage("aggfct.png")));
      MenuItem sum = new MenuItem(_i18n.t("agg.function.sum"), new ImageView(new Props(getClass()).getImage("aggsum.png")));
      MenuItem max = new MenuItem(_i18n.t("agg.function.max"), new ImageView(new Props(getClass()).getImage("aggmax.png")));
      MenuItem min = new MenuItem(_i18n.t("agg.function.min"), new ImageView(new Props(getClass()).getImage("aggmin.png")));
      MenuItem count = new MenuItem(_i18n.t("agg.function.count"), new ImageView(new Props(getClass()).getImage("aggcount.png")));

      ContextMenu popup = new ContextMenu(none, sum, max, min, count);

      Point2D localToScene = localToScreen(0, 0);

      popup.show(AppState.get().getPrimaryStage(), localToScene.getX(), localToScene.getY());
   }

   private void updateGraphics()
   {
      if(_enabled)
      {
         setCenter(_imageEnabled);
      }
      else
      {
         setCenter(_imageDisabled);
      }
   }


   public void setEnabled(boolean b)
   {
      _enabled = b;
      updateGraphics();
   }
}
