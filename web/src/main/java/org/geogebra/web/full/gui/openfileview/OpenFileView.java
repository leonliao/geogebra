package org.geogebra.web.full.gui.openfileview;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.main.Feature;
import org.geogebra.common.main.OpenFileListener;
import org.geogebra.common.move.events.BaseEvent;
import org.geogebra.common.move.ggtapi.events.LogOutEvent;
import org.geogebra.common.move.ggtapi.events.LoginEvent;
import org.geogebra.common.move.ggtapi.models.Chapter;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.common.move.ggtapi.models.Material.Provider;
import org.geogebra.common.move.ggtapi.models.MaterialRequest.Order;
import org.geogebra.common.move.ggtapi.requests.MaterialCallbackI;
import org.geogebra.common.move.views.EventRenderable;
import org.geogebra.common.util.AsyncOperation;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.full.gui.MyHeaderPanel;
import org.geogebra.web.full.gui.dialog.DialogManagerW;
import org.geogebra.web.full.main.BrowserDevice.FileOpenButton;
import org.geogebra.web.html5.gui.FastClickHandler;
import org.geogebra.web.html5.gui.util.NoDragImage;
import org.geogebra.web.html5.gui.util.StandardButton;
import org.geogebra.web.html5.gui.view.browser.BrowseViewI;
import org.geogebra.web.html5.gui.view.browser.MaterialListElementI;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.shared.ggtapi.LoginOperationW;
import org.geogebra.web.shared.ggtapi.models.MaterialCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * View for browsing materials
 * 
 * @author Alicia
 *
 */
public class OpenFileView extends MyHeaderPanel
		implements BrowseViewI, OpenFileListener, EventRenderable {
	private static final int HEADER_HEIGHT = 44;
	/**
	 * application
	 */
	protected AppW app;
	// header
	private FlowPanel headerPanel;
	private StandardButton backBtn;
	private Label headerCaption;

	// content panel
	private FlowPanel contentPanel;
	// button panel
	private FlowPanel buttonPanel;
	private StandardButton newFileBtn;
	private FileOpenButton openFileBtn;

	// dropdown
	private ListBox sortDropDown;

	// material panel
	private FlowPanel materialPanel;
	private MaterialCallbackI ggtMaterialsCB;
	private MaterialCallbackI userMaterialsCB;
	private MaterialCallbackI sharedMaterialsCB;
	// info panel
	private FlowPanel infoPanel;
	private Label caption;
	private Label info;

	private boolean[] materialListEmpty = { true, true };
	private static final int TYPE_USER = 0;
	private static final int TYPE_SHARED = 1;

	private Order order = Order.timestamp;
	private static Order[] map = new Order[] { Order.title, Order.created,
			Order.timestamp };

	/**
	 * @param app
	 *            application
	 * @param openFileButton
	 *            button to open file picker
	 */
	public OpenFileView(AppW app, FileOpenButton openFileButton) {
		this.app = app;
		this.openFileBtn = openFileButton;
		if (this.app.getLoginOperation() == null) {
			this.app.initSignInEventFlow(new LoginOperationW(app),
					true);
		}
		this.app.getLoginOperation().getView().add(this);
		initGUI();
	}

	private void initGUI() {
		this.setStyleName("openFileView");
		this.userMaterialsCB = getUserMaterialsCB(TYPE_USER);
		this.sharedMaterialsCB = getUserMaterialsCB(TYPE_SHARED);
		this.ggtMaterialsCB = getGgtMaterialsCB();
		initHeader();
		initContentPanel();
		initButtonPanel();
		initSortDropdown();
		initMaterialPanel();
	}

	/**
	 * adds content if available, notification otherwise
	 */
	protected void addContent() {
		contentPanel.clear();
		if (materialListEmpty[TYPE_USER] && materialListEmpty[TYPE_SHARED]) {
			showEmptyListNotification();
			setExtendedButtonStyle();
			infoPanel.add(buttonPanel);
		} else {
			setSmallButtonStyle();
			contentPanel.add(buttonPanel);
			contentPanel.add(sortDropDown);
			contentPanel.add(materialPanel);
		}
	}

	private void initHeader() {
		headerPanel = new FlowPanel();
		headerPanel.setStyleName("openFileViewHeader");

		backBtn = new StandardButton(
				MaterialDesignResources.INSTANCE.mow_back_arrow(),
				null, 24,
				app);
		backBtn.setStyleName("headerBackButton");
		backBtn.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				close();
			}
		});
		headerPanel.add(backBtn);

		headerCaption = new Label(
				localize("mow.openFileViewTitle"));
		headerCaption.setStyleName("headerCaption");
		headerPanel.add(headerCaption);

		this.setHeaderWidget(headerPanel);
	}

	private void initContentPanel() {
		contentPanel = new FlowPanel();
		contentPanel.setStyleName("fileViewContentPanel");
		this.setContentWidget(contentPanel);
	}

	private void initButtonPanel() {
		buttonPanel = new FlowPanel();
		newFileBtn = new StandardButton(
				MaterialDesignResources.INSTANCE.file_plus(),
				localize("mow.newFile"), 18, app);
		newFileBtn.addFastClickHandler(new FastClickHandler() {

			@Override
			public void onClick(Widget source) {
				newFile();
			}
		});
		openFileBtn.setImageAndText(
				MaterialDesignResources.INSTANCE.mow_pdf_open_folder()
						.getSafeUri().asString(),
				localize("mow.offlineMyFiles"));
		buttonPanel.add(openFileBtn);
		buttonPanel.add(newFileBtn);
	}

	private void initSortDropdown() {
		sortDropDown = new ListBox();
		sortDropDown.setMultipleSelect(false);
		sortDropDown.addItem(localize("SortBy"));
		sortDropDown.getElement().getFirstChildElement()
				.setAttribute("disabled", "disabled");
		for (int i = 0; i < map.length; i++) {
			sortDropDown.addItem(localize(labelFor(map[i])));
		}
		sortDropDown.setSelectedIndex(3);
		sortDropDown.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				updateOrder();
			}
		});
	}

	private static String labelFor(Order order2) {
		switch (order2) {
		case created:
			return "sort_date_created";
		case timestamp:
			return "sort_last_modified";
		default:
		case title:
			return "sort_title";
		}
	}

	/**
	 * Reload materials sorted by another property.
	 */
	protected void updateOrder() {

		order = map[sortDropDown.getSelectedIndex() - 1];
		loadAllMaterials();
	}

	private void initMaterialPanel() {
		materialPanel = new FlowPanel();
		materialPanel.addStyleName("materialPanel");
	}

	private String localize(String id) {
		return app.getLocalization().getMenu(id);
	}

	/**
	 * start a new file
	 */
	protected void newFile() {
		AsyncOperation<Boolean> newConstruction = new AsyncOperation<Boolean>() {

			@Override
			public void callback(Boolean active) {
				app.setWaitCursor();
				app.fileNew();
				app.setDefaultCursor();

				if (!app.isUnbundledOrWhiteboard()) {
					app.showPerspectivesPopup();
				}
				if (app.has(Feature.MOW_MULTI_PAGE)
						&& app.getPageController() != null) {
					app.getPageController().resetPageControl();
				}

			}
		};
		app.getArticleElement().attr("perspective", "");
		((DialogManagerW) getApp().getDialogManager()).getSaveDialog()
				.showIfNeeded(newConstruction);
		close();
	}

	/**
	 * @param fileToHandle
	 *            JS file object
	 * @param callback
	 *            callback after file is open
	 */
	public void openFile(final JavaScriptObject fileToHandle,
			final JavaScriptObject callback) {
		if (app.getLAF().supportsLocalSave()) {
			app.getFileManager().setFileProvider(Provider.LOCAL);
		}
		app.openFile(fileToHandle, callback);
		close();
	}

	private void showEmptyListNotification() {
		contentPanel.clear();
		infoPanel = new FlowPanel();
		infoPanel.setStyleName("emptyMaterialListInfo");
		Image image = new NoDragImage(
				MaterialDesignResources.INSTANCE.mow_lightbulb(), 112, 112);
		// init texts
		caption = new Label(localize("emptyMaterialList.caption.mow"));
		caption.setStyleName("caption");
		info = new Label(localize("emptyMaterialList.info.mow"));
		info.setStyleName("info");
		// build panel
		infoPanel.add(image);
		infoPanel.add(caption);
		infoPanel.add(info);
		// add panel to content panel
		contentPanel.add(infoPanel);
	}

	private void setExtendedButtonStyle() {
		newFileBtn.setStyleName("extendedFAB");
		newFileBtn.addStyleName("FABteal");
		newFileBtn.addStyleName("buttonMargin24");
		openFileBtn.setStyleName("extendedFAB");
		openFileBtn.addStyleName("FABwhite");
		buttonPanel.setStyleName("fileViewButtonPanel");
		buttonPanel.addStyleName("center");
	}

	private void setSmallButtonStyle() {
		newFileBtn.setStyleName("containedButton");
		newFileBtn.addStyleName("buttonMargin16");
		openFileBtn.setStyleName("containedButton");
		buttonPanel.setStyleName("fileViewButtonPanel");
	}

	@Override
	public AppW getApp() {
		return app;
	}

	@Override
	public void resizeTo(int width, int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setMaterialsDefaultStyle() {
		if (materialPanel.getWidgetCount() == 0) {
			updateMaterials();
		}
	}

	@Override
	public void loadAllMaterials() {
		clearMaterials();
		if (this.app.getLoginOperation().isLoggedIn()) {
			app.getLoginOperation().getGeoGebraTubeAPI()
					.getUsersOwnMaterials(this.userMaterialsCB,
							order);
			app.getLoginOperation().getGeoGebraTubeAPI()
					.getSharedMaterials(this.sharedMaterialsCB, order);
		} else {
			app.getLoginOperation().getGeoGebraTubeAPI()
					.getFeaturedMaterials(this.ggtMaterialsCB);
		}
	}

	@Override
	public void clearMaterials() {
		materialPanel.clear();
	}

	private void clearPanels() {
		if (contentPanel != null) {
			contentPanel.clear();
		}
		if (infoPanel != null) {
			infoPanel.clear();
		}
	}

	/**
	 * update material list
	 */
	public void updateMaterials() {
		clearPanels();
		loadAllMaterials();
	}

	@Override
	public void disableMaterials() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onSearchResults(List<Material> response,
			ArrayList<Chapter> chapters) {
		// TODO Auto-generated method stub
	}

	@Override
	public void displaySearchResults(String query) {
		// TODO Auto-generated method stub
	}

	@Override
	public void refreshMaterial(Material material, boolean isLocal) {
		// TODO Auto-generated method stub
	}

	@Override
	public void rememberSelected(MaterialListElementI materialElement) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setLabels() {
		headerCaption.setText(localize("mow.openFileViewTitle"));
		newFileBtn.setText(localize("mow.newFile"));
		openFileBtn
				.setImageAndText(
						MaterialDesignResources.INSTANCE.mow_pdf_open_folder()
								.getSafeUri().asString(),
						localize("mow.offlineMyFiles"));
		if (sortDropDown != null) {
			sortDropDown.setItemText(0, localize("SortBy"));
			for (int i = 0; i < map.length; i++) {
				sortDropDown.setItemText(i + 1, localize(labelFor(map[i])));
			}
		}
		if (infoPanel != null) {
			caption.setText(localize("emptyMaterialList.caption.mow"));
			info.setText(localize("emptyMaterialList.info.mow"));
		}
	}

	@Override
	public void addMaterial(Material material) {
		for (int i = 0; i < materialPanel.getWidgetCount(); i++) {
			Widget wgt = materialPanel.getWidget(i);
			if (wgt instanceof MaterialCard
					&& isBefore(material, ((MaterialCard) wgt).getMaterial())) {
				if (((MaterialCard) wgt).getMaterial().getSharingKeyOrId()
						.equals(material.getSharingKeyOrId())) {
					// don't add the same material twice
					return;
				}
				materialPanel.insert(new MaterialCard(material, app), i);
				return;
			}
		}
		materialPanel.add(new MaterialCard(material, app));
	}

	private boolean isBefore(Material material, Material material2) {
		switch (order) {
		case title:
			return material.getTitle().compareTo(material2.getTitle()) <= 0;
		case created:
			return material.getDateCreated() > material2.getDateCreated();
		case timestamp:
			return material.getTimestamp() > material2.getTimestamp();
		default:
			return false;
		}

	}

	@Override
	public void removeMaterial(Material material) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onOpenFile() {
		// TODO
		return false;
	}

	private MaterialCallback getUserMaterialsCB(final int type) {
		return new MaterialCallback() {

			@Override
			public void onLoaded(final List<Material> parseResponse,
					ArrayList<Chapter> meta) {
				addUsersMaterials(parseResponse, type);
				addContent();
			}
		};
	}

	/**
	 * Adds the given {@link Material materials}.
	 * 
	 * @param matList
	 *            List of materials
	 * @param type
	 *            TYPE_USER or TYPE_SHARED
	 */
	protected void addUsersMaterials(final List<Material> matList, int type) {
		materialListEmpty[type] = matList.isEmpty();
		for (int i = 0; i < matList.size(); i++) {
			addMaterial(matList.get(i));
		}
	}

	private MaterialCallback getGgtMaterialsCB() {
		return new MaterialCallback() {
			@Override
			public void onError(final Throwable exception) {
				exception.printStackTrace();
				Log.warn(exception.getMessage());
			}

			@Override
			public void onLoaded(final List<Material> response,
					ArrayList<Chapter> meta) {
				addGGTMaterials(response, meta);
				addContent();
			}
		};
	}

	/**
	 * adds the new materials (matList) - GeoGebraTube only
	 * 
	 * @param matList
	 *            List of materials
	 * @param chapters
	 *            list of book chapters
	 */
	public final void addGGTMaterials(final List<Material> matList,
			final ArrayList<Chapter> chapters) {
		materialListEmpty[TYPE_USER] = matList.isEmpty();
		if (chapters == null || chapters.size() < 2) {
			for (final Material mat : matList) {
				addMaterial(mat);
			}
		}
	}

	@Override
	public void setHeaderVisible(boolean visible) {
		if (headerPanel.getElement().getParentElement() != null) {
			headerPanel.getElement().getParentElement().getStyle()
					.setDisplay(visible ? Display.BLOCK : Display.NONE);
		}
		contentPanel.getElement().getStyle().setTop(visible ? HEADER_HEIGHT : 0,
				Unit.PX);
	}

	@Override
	public void renderEvent(BaseEvent event) {
		if (event instanceof LoginEvent || event instanceof LogOutEvent) {
			updateMaterials();
			if (event instanceof LogOutEvent) {
				close();
			}
		}
	}

}
