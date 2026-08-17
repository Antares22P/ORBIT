package com.orbit.api;

import com.orbit.model.Member;
import com.orbit.service.MemberService;

import java.util.Map;

public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // Get all members
    public Map<String, Member> getMembers(
            String groupId) {

        return memberService.getMembers(
                groupId
        );
    }


    // Get one member
    public Member getMember(
            String groupId,
            String memberId) {

        return memberService.getMember(
                groupId,
                memberId
        );
    }


    // Add member
    public Member addMember(
            String groupId,
            String memberId,
            String name,
            String avatar,
            String color) {

        return memberService.createMember(
                groupId,
                memberId,
                name,
                avatar,
                color
        );
    }


    // Remove member
    public boolean removeMember(
            String groupId,
            String memberId) {

        return memberService.removeMember(
                groupId,
                memberId
        );
    }


    // Update member location
    public boolean updateLocation(
            String groupId,
            String memberId,
            double latitude,
            double longitude,
            double accuracy,
            double speed,
            double heading) {

        return memberService.updateLocation(
                groupId,
                memberId,
                latitude,
                longitude,
                accuracy,
                speed,
                heading
        );
    }
}