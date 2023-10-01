package com.mypay.remittance.adapter.out.service.membership;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mypay.common.CommonHttpClient;
import com.mypay.common.ExternalSystemAdapter;
import com.mypay.remittance.application.port.out.membership.MembershipPort;
import com.mypay.remittance.application.port.out.membership.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@ExternalSystemAdapter
@RequiredArgsConstructor
public class MembershipServiceAdapter implements MembershipPort {

    private final CommonHttpClient membershipServiceHttpClient;

    @Value("${service.membership.url}")
    private String membershipServiceEndpoint;

    @Override
    public MembershipStatus getMembershipStatus(String membershipId) {
        String url = String.join("/", this.membershipServiceEndpoint, "membership", membershipId);

        try {
            String jsonResponse = membershipServiceHttpClient.sendGetRequest(url).body();
            ObjectMapper mapper = new ObjectMapper();

            Membership membership = mapper.readValue(jsonResponse, Membership.class);
            if (membership.isValid()) {
                return new MembershipStatus(membership.getMembershipId(), true);
            } else {
                return new MembershipStatus(membership.getMembershipId(), false);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
