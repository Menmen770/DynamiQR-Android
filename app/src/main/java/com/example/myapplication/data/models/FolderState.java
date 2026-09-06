package com.example.myapplication.data.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderState {

    @SerializedName("folders")
    private List<FolderItem> folders;

    @SerializedName("assignments")
    private Map<String, String> assignments;

    @SerializedName("globalOrder")
    private List<String> globalOrder;

    @SerializedName("folderOrders")
    private Map<String, List<String>> folderOrders;

    public List<FolderItem> getFolders() {
        return folders != null ? folders : new ArrayList<>();
    }

    public void setFolders(List<FolderItem> folders) {
        this.folders = folders;
    }

    public Map<String, String> getAssignments() {
        if (assignments == null) {
            assignments = new HashMap<>();
        }
        return assignments;
    }

    public void setAssignments(Map<String, String> assignments) {
        this.assignments = assignments;
    }

    public List<String> getGlobalOrder() {
        return globalOrder != null ? globalOrder : new ArrayList<>();
    }

    public void setGlobalOrder(List<String> globalOrder) {
        this.globalOrder = globalOrder;
    }

    public Map<String, List<String>> getFolderOrders() {
        if (folderOrders == null) {
            folderOrders = new HashMap<>();
        }
        return folderOrders;
    }

    public void setFolderOrders(Map<String, List<String>> folderOrders) {
        this.folderOrders = folderOrders;
    }

    public static class FolderItem {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        public FolderItem() {
        }

        public FolderItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
