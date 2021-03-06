	/*Copyright 2016 stoneriver
 *
 *Licensed under the Apache License, Version 2.0 (the "License");
 *you may not use this file except in compliance with the License.
 *You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *Unless required by applicable law or agreed to in writing, software
 *distributed under the License is distributed on an "AS IS" BASIS,
 *WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *See the License for the specific language governing permissions and
 *limitations under the License.
 */
package com.github.stoneriver.onkyohelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class OnkyoHelperController implements Initializable {
	
	//変数宣言
	//コントローラ使用分変数
	private int currentEventNum;
	private Plan plan;
	private Properties config = new Properties();
	
	//GUI使用分変数
	//イベントリスト
	private ObservableList<Event> eventList;
	@FXML
	private TableView<Event> tableEventList;
	@FXML
	private TableColumn<Event, Integer> columnEventNum;
	@FXML
	private TableColumn<Event, String> columnEventName;
	
	//直近イベントリスト
	private ObservableList<Event> nearbyEventList;
	@FXML
	private TableView<Event> tableNearbyEventList;
	@FXML
	private TableColumn<Event, Integer> columnNearbyEventNum;
	@FXML
	private TableColumn<Event, String> columnNearbyEventStatus;
	@FXML
	private TableColumn<Event, String> columnNearbyEventName;
	@FXML
	private TableColumn<Event, Integer> columnNearbyEventStart;
	@FXML
	private Label label1;
	@FXML
	private Label label2;
	@FXML
	private Label label3;
	@FXML
	private Label label4;
	
	//ボリュームコントロール
	@FXML
	private Button buttonClimax;
	@FXML
	private Button buttonNormal;
	@FXML
	private Button buttonSilent;
	
	//イベント進行
	@FXML
	private Button buttonBack;
	@FXML
	private Button buttonForward;
	
	//メニュー
	@FXML
	private Menu menuOpen;
	private MenuItem menuItemUpdate;
	@FXML
	private MenuItem menuItemAbout;
	
	//関数定義
	//クラス定義
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setDisableAll(true);
		initializeNearbyEventList();
		initializeEventList();
		try {
			loadConfig();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		menuItemUpdate = new MenuItem(Messages.getString("OnkyoHelperController.Update")); //$NON-NLS-1$
		menuItemUpdate.setOnAction((ActionEvent e) -> {
			updateAvailablePlans();
		});
		updateAvailablePlans();
	}
	
	private void addPlansToMenu(String[] plans) {
		for (String s : plans) {
			MenuItem item = new MenuItem(s);
			item.setOnAction((ActionEvent e) -> {
				loadPlan(s);
			});
			menuOpen.getItems().add(item);
		}
	}
	
	private void loadPlan(String property) {
		try {
			plan = new Plan(property);
		} catch (IOException e) {
			e.printStackTrace();
		}
		setDisableAll(false);
		updateEventList();
		setCurrentEvent(0);
	}
	
	private void updateAvailablePlans() {
		String[] plans = OnkyoHelperCollections.findAllPlans();
		menuOpen.getItems().clear();
		menuOpen.getItems().add(menuItemUpdate);
		addPlansToMenu(plans);
	}
	
	private void loadConfig() throws IOException {
		File file = new File("OnkyoHelper.properties"); //$NON-NLS-1$
		InputStream inputStream;
		if(file.exists()) {
			inputStream = new FileInputStream(file);
			config.load(inputStream);
		} else {
			file.createNewFile();
			inputStream = new FileInputStream(file);
			config.load(inputStream);
			config.setProperty("VolumeNormal", "0.7"); //$NON-NLS-1$ //$NON-NLS-2$
			config.setProperty("VolumeClimax", "1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	//コントローラ関数
	private void setCurrentEvent(int num) {
		
		plan.getEvent(currentEventNum).stop();
		
		currentEventNum = num;
		//ボタンの有効無効
		if (currentEventNum <= 0) {
			buttonBack.setDisable(true);
		} else {
			buttonBack.setDisable(false);
		}
		if (currentEventNum >= plan.getEventCount() - 1) {
			buttonForward.setDisable(true);
		} else {
			buttonForward.setDisable(false);
		}
		
		plan.getEvent(currentEventNum).play();
		updateNearbyEventList();
	}
	
	//GUI関数
	private void setDisableAll(boolean b) {
		for (Control c : getAllControl()) {
			c.setDisable(b);
		}
	}
	
	private Control[] getAllControl() {
		Control[] result = {
				buttonClimax,
				buttonNormal,
				buttonSilent,
				buttonBack,
				buttonForward,
				tableNearbyEventList,
				tableEventList
		};
		return result;
	}
	
	private void initializeNearbyEventList() {
		nearbyEventList = FXCollections.observableArrayList();
		columnNearbyEventNum.setCellValueFactory(new PropertyValueFactory<>("num")); //$NON-NLS-1$
		columnNearbyEventStatus.setCellValueFactory(new PropertyValueFactory<>("status")); //$NON-NLS-1$
		columnNearbyEventName.setCellValueFactory(new PropertyValueFactory<>("name")); //$NON-NLS-1$
		columnNearbyEventStart.setCellValueFactory(new PropertyValueFactory<>("start")); //$NON-NLS-1$
		tableNearbyEventList.setItems(nearbyEventList);
	}
	
	private void initializeEventList() {
		eventList = FXCollections.observableArrayList();
		columnEventNum.setCellValueFactory(new PropertyValueFactory<>("num")); //$NON-NLS-1$
		columnEventName.setCellValueFactory(new PropertyValueFactory<>("name")); //$NON-NLS-1$
		tableEventList.setItems(eventList);
	}
	
	private void updateNearbyEventList() {
		nearbyEventList.clear();
		int fix;
		if (currentEventNum == 0) {
			fix = 0;
		} else if (currentEventNum == plan.getEventCount() - 2) {
			fix = -2;
		} else if (currentEventNum == plan.getEventCount() - 1) {
			fix = -3;
		} else {
			fix = -1;
		}
		for (int i = fix + currentEventNum; i < fix + currentEventNum + 4; i++) {
			nearbyEventList.add(plan.getEvent(i));
		}
		
	}
	
	private void updateEventList() {
		for (Event e : plan.getEvents()) {
			eventList.add(e);
		}
	}
	
	//イベント関数
	@FXML
	private void onButtonClimaxAction() {
		plan.getEvent(currentEventNum).setPreferredVolume(Double.parseDouble(config.getProperty("VolumeClimax"))); //$NON-NLS-1$
	}
	
	@FXML
	private void onButtonNormalAction() {
		plan.getEvent(currentEventNum).setPreferredVolume(Double.parseDouble(config.getProperty("VolumeNormal"))); //$NON-NLS-1$
	}
	
	@FXML
	private void onButtonSilentAction() {
		plan.getEvent(currentEventNum).setPreferredVolume(0.0);
	}
	
	@FXML
	private void onButtonBackAction() {
		setCurrentEvent(currentEventNum - 1);
	}
	
	@FXML
	private void onButtonForwardAction() {
		setCurrentEvent(currentEventNum + 1);
	}
	
	@FXML
	private void onMenuItemUpdateAction() {
		updateAvailablePlans();
	}
	
	@FXML
	private void onMenuItemAboutAction() {
		Alert a = new Alert(AlertType.INFORMATION);
		a.setHeaderText(Messages.getString("OnkyoHelperController.About0")); //$NON-NLS-1$
		a.setContentText(Messages.getString("Main.Version") + "\n" //$NON-NLS-1$ //$NON-NLS-2$
				+ Messages.getString("OnkyoHelperController.About1") //$NON-NLS-1$
				+ Messages.getString("OnkyoHelperController.About2") //$NON-NLS-1$
				+ Messages.getString("OnkyoHelperController.About3")); //$NON-NLS-1$
		a.show();
	}
	
}
