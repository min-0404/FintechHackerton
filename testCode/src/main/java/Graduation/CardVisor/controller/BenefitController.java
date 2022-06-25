package Graduation.CardVisor.controller;


import Graduation.CardVisor.domain.serviceone.ServiceOneDto;
import Graduation.CardVisor.domain.servicetwo.ServiceTwoCardsDto;
import Graduation.CardVisor.domain.servicetwo.ServiceTwoDto;
import Graduation.CardVisor.service.BenefitService;
import Graduation.CardVisor.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RestController
@RequiredArgsConstructor
public class BenefitController {

    @Autowired
    private BenefitService benefitService;



    // 추천 서비스 1 : 프론트에서 선택된 혜택들을 받아주는 컨트롤러
    @CrossOrigin("http://localhost:3000") // method: post 로 받을 시 꼭 붙여줄 것
    @PostMapping("/select")
    public void getSelections(@RequestBody List<ServiceOneDto> list){ // [{"memberId" : 1, "brandName": transport_bus}, {}, {}...] 형식으로 담겨져옴
        benefitService.saveSelections(list);
    }

    // 추천 서비스 1: 추천된 카드들의 정보를 프론트로 전송하는 컨트롤러
    @GetMapping("/results")
    public Map<String, Object> results() {

        // 규칙 : 컨트롤러 함수의 반환형은 무조건 HashMap
        Map<String, Object> store = new HashMap<>();

        store.put("topTenCards", benefitService.dtoToRecommendedCards());

        store.put("bestCardBenefits", benefitService.bestCardBenefits());

        //store.put("likeCount", benefitService.bestCardLikeCount());

        return store;
    }

    // serviceTwo 테이블에 무작위로 200개 저장해주는 함수
    @GetMapping("/serviceTwo/save")
    public ServiceTwoDto serviceTwoSave() {

        return benefitService.DtoToServiceTwo();
    }

    // 얘는 이제 필요없음
    @GetMapping("/serviceTwo/recommend")
    public ServiceTwoCardsDto serviceTwoRecommend() {

        return benefitService.flaskServiceTwoRecommend();
    }

    // 추천 서비스2: 추천된 카드들의 정보를 프론트로 전송하는 컨트롤러
    @GetMapping("/results2")
    public Map<String, Object> results2(){

        benefitService.save();

        Map<String, Object> store = new HashMap<>();

        store.put("topTenCards", benefitService.dtoToRecommendedCards2());

        store.put("bestCardBenefits", benefitService.bestCardBenefits2());

        return store;

    }

}
