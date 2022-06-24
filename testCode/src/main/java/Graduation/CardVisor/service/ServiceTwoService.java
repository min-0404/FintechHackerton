package Graduation.CardVisor.service;

import Graduation.CardVisor.domain.Brand;
import Graduation.CardVisor.domain.Card;
import Graduation.CardVisor.domain.benefit.Benefit;
import Graduation.CardVisor.domain.serviceone.ServiceOneDto;
import Graduation.CardVisor.domain.servicetwo.ServiceTwo;
import Graduation.CardVisor.domain.servicetwo.ServiceTwoDto;
import Graduation.CardVisor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ServiceTwoService {

    @Autowired
    private ServiceTwoRepository serviceTwoRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    public ServiceTwo dtoToServiceTwo(ServiceTwoDto serviceTwoDto) {
        ServiceTwo serviceTwo = new ServiceTwo();
        serviceTwo.setMember(memberRepository.getById(serviceTwoDto.getMemberId()));
        serviceTwo.setBrand(brandRepository.getByNameEngish(serviceTwoDto.getBrandName()));
        serviceTwo.setCost(serviceTwoDto.getCost());
        return serviceTwoRepository.save(serviceTwo);
    }

    public void saveServiceTwoListToDB(List<ServiceTwoDto> list){

        // 1단계: 일단, 프론트엔드에서 받아온 ServiceOneDto 를 ServiceOne 객체로 바꿔줘서 데이터베이스에 저장한다.
        for(ServiceTwoDto serviceTwoDto : list){
            dtoToServiceTwo(serviceTwoDto); // Dto 를 정식 ServiceTwo 객체로 변환해줌
        }
    }

    public List<Card> serviceTwoResultCardListResults(Long memberId) {
        List<ServiceTwo> consumptionDetails = serviceTwoRepository.findAllByMemberId(memberId);
        List<Brand> consumptionDetailsBrands = new ArrayList<>();
        for(ServiceTwo serviceTwo : consumptionDetails) {
            consumptionDetailsBrands.add(serviceTwo.getBrand());
        }

        List<Card> cardsWithBrands = benefitRepository.findCardByBrand(consumptionDetailsBrands);

        for(Card card : cardsWithBrands) {
            List<Benefit> benefitList = benefitRepository.findAllByCard(card);
            for(Benefit benefit : benefitList) {

            }
        }
    }
}
