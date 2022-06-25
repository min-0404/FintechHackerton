package Graduation.CardVisor.service;

import Graduation.CardVisor.domain.kakao.SearchLocalRes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BenefitServiceTest {

    @Autowired
    private BenefitService benefitService;

    @Test
    public SearchLocalRes kakaoSearchLocal() {

        SearchLocalRes searchLocalRes = new SearchLocalRes();
        searchLocalRes = benefitService.kakaoSearchLocal("투썸플레이스 홍대입구역점");
        return searchLocalRes;
    }
}