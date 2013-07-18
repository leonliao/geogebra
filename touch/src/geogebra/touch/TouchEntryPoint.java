package geogebra.touch;

import geogebra.common.move.ggtapi.models.Material;
import geogebra.html5.js.ResourcesInjector;
import geogebra.touch.gui.BrowseGUI;
import geogebra.touch.gui.GuiResources;
import geogebra.touch.gui.TabletGUI;
import geogebra.touch.gui.WorksheetGUI;
import geogebra.touch.gui.elements.ProgressIndicator;
import geogebra.touch.gui.laf.AndroidLAF;
import geogebra.touch.gui.laf.AppleLAF;
import geogebra.touch.gui.laf.DefaultLAF;
import geogebra.touch.gui.laf.LookAndFeel;
import geogebra.touch.gui.laf.WindowsStoreLAF;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.googlecode.gwtphonegap.client.PhoneGap;
import com.googlecode.gwtphonegap.client.event.BackButtonPressedEvent;
import com.googlecode.gwtphonegap.client.event.BackButtonPressedHandler;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class TouchEntryPoint implements EntryPoint {
	public static ProgressIndicator progressIndicator = new ProgressIndicator();

	static TabletDeckLayoutPanel appWidget = new TabletDeckLayoutPanel();
	static TabletGUI tabletGUI = new TabletGUI();
	static BrowseGUI browseGUI;
	static WorksheetGUI worksheetGUI;

	static final PhoneGap phoneGap = (PhoneGap) GWT.create(PhoneGap.class);
	private static LookAndFeel laf;

	@Override
	public void onModuleLoad() {
		progressIndicator.show();

		loadMobileAsync();

		// insert mathquill css
		String mathquillcss = GuiResources.INSTANCE.mathquillCss().getText();
		StyleInjector.inject(mathquillcss);
	}

	private static void loadMobileAsync() {
		GWT.runAsync(new RunAsyncCallback() {
			@Override
			public void onSuccess() {
				ResourcesInjector.injectResources();
				setLookAndFeel();
				TouchApp app = new TouchApp(TouchEntryPoint.tabletGUI);
				FileManagerM fm = new FileManagerM();
				browseGUI = new BrowseGUI(app, fm);
				worksheetGUI = new WorksheetGUI(app, fm);
				TouchEntryPoint.appWidget.add(TouchEntryPoint.tabletGUI);
				TouchEntryPoint.appWidget.add(TouchEntryPoint.browseGUI);
				TouchEntryPoint.appWidget.add(TouchEntryPoint.worksheetGUI);

				RootLayoutPanel.get().add(TouchEntryPoint.appWidget);

				app.start(fm);

				TouchEntryPoint.showTabletGUI();

				Window.addResizeHandler(new ResizeHandler() {

					@Override
					public void onResize(ResizeEvent event) {
						// TouchEntryPoint.appWidget.setPixelSize(event.getWidth(),
						// event.getHeight());
					}
				});

				tabletGUI.getContentWidget().getElement().getStyle()
						.setOverflow(Overflow.VISIBLE);
				app.getScriptManager().ggbOnInit();
				// needed for testing
				if (RootPanel.getBodyElement()
						.getAttribute("data-param-ggbbase64").length() > 0) {
					app.getGgbApi().setBase64(
							RootPanel.getBodyElement().getAttribute(
									"data-param-ggbbase64"));
				}

				initPhoneGap();

				Window.enableScrolling(false);

				progressIndicator.hide();

			}

			private void initPhoneGap() {
				// phoneGap.addHandler(new PhoneGapAvailableHandler()
				// {
				// @Override
				// public void onPhoneGapAvailable(PhoneGapAvailableEvent event)
				// {
				// // TODO Auto-generated method stub
				// System.out.println("test onPhoneGapAvailable");
				// }
				// });
				//
				// phoneGap.addHandler(new PhoneGapTimeoutHandler()
				// {
				//
				// @Override
				// public void onPhoneGapTimeout(PhoneGapTimeoutEvent event)
				// {
				// // TODO Auto-generated method stub
				//
				// }
				// });
				phoneGap.initializePhoneGap();
				phoneGap.getEvent()
						.getBackButton()
						.addBackButtonPressedHandler(
								new BackButtonPressedHandler() {

									@Override
									public void onBackButtonPressed(
											BackButtonPressedEvent event) {
										goBack();
									}
								});
			}

			@Override
			public void onFailure(Throwable reason) {
				// App.debug(reason);
				reason.printStackTrace();
			}
		});
	}

	public static void goBack() {
		// is Dialog open? -> close dialog
		if (tabletGUI.getTouchModel().getGuiModel().isDialogShown()) {
			tabletGUI.getTouchModel().getGuiModel().closeActiveDialog();
		} else {
			// else go to last view in history
			if (!appWidget.goBack()) {
				// if history is empty -> close app
				phoneGap.exitApp();
			}
		}
		tabletGUI.getTouchModel().getKernel().notifyRepaint();
		// tabletGUI.getTouchModel().getKernel().getApplication().setSaved();
		if (laf.getTabletHeaderPanel() != null) {
			laf.getTabletHeaderPanel().enableDisableButtons();
		}
	}

	public static void showTabletGUI() {
		TouchEntryPoint.appWidget.showWidget(TouchEntryPoint.tabletGUI);
		tabletGUI.getTouchModel().getKernel().notifyRepaint();
		if (laf.getTabletHeaderPanel() != null) {
			laf.getTabletHeaderPanel().enableDisableButtons();
		}
	}

	public static void showBrowseGUI() {
		TouchEntryPoint.appWidget.showWidget(TouchEntryPoint.browseGUI);
	}

	public static void showWorksheetGUI(Material material) {
		TouchEntryPoint.worksheetGUI.loadWorksheet(material);
		TouchEntryPoint.appWidget.showWidget(TouchEntryPoint.worksheetGUI);
	}

	public static void reloadLocalFiles() {
		TouchEntryPoint.browseGUI.reloadLocalFiles();
	}

	protected static void setLookAndFeel() {
		String param = RootPanel.getBodyElement()
				.getAttribute("data-param-laf");

		if ("android".equals(param)) {
			laf = new AndroidLAF();
		} else if ("apple".equals(param)) {
			laf = new AppleLAF();
		} else if ("win".equals(param)) {
			laf = new WindowsStoreLAF();
		} else {
			laf = new DefaultLAF();
		}
	}

	public static LookAndFeel getLookAndFeel() {
		return TouchEntryPoint.laf;
	}

	public static void allowEditing(boolean b, Material material) {
		tabletGUI.allowEditing(b, material);
	}
}