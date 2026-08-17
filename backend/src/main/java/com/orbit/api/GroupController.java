package com.orbit.api;

import com.orbit.model.Group;
import com.orbit.service.GroupService;

public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // =========================================================
    // CREATE GROUP
    // =========================================================

    public Group createGroup(
            String groupId,
            String groupName) {

        return groupService.createGroup(
                groupId,
                groupName
        );
    }


    // =========================================================
    // GET GROUP
    // =========================================================

    public Group getGroup(
            String groupId) {

        return groupService.getGroup(
                groupId
        );
    }


    // =========================================================
    // GROUP EXISTS
    // =========================================================

    public boolean groupExists(
            String groupId) {

        return groupService.groupExists(
                groupId
        );
    }


    // =========================================================
    // DELETE GROUP
    // =========================================================

    public void deleteGroup(
            String groupId) {

        groupService.deleteGroup(
                groupId
        );
    }
}