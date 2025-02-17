package org.diylc.core.gerber;

import java.util.List;
import org.diylc.core.GerberLayer;
import org.diylc.core.ILayeredComponent;
import com.bancika.gerberwriter.DataLayer;

public interface IGerberComponent extends ILayeredComponent {

  void drawToGerber(DataLayer dataLayer);
  
  List<GerberLayer> getGerberLayers();
}
