package Graduation.CardVisor.service;


import Graduation.CardVisor.domain.Card;
import Graduation.CardVisor.domain.benefit.BenefitDto;
import Graduation.CardVisor.domain.kakao.SearchLocalRes;
import Graduation.CardVisor.domain.serviceThree.ServiceThree;
import Graduation.CardVisor.domain.serviceThree.ServiceThreeCardsDto;
import Graduation.CardVisor.domain.serviceThree.ServiceThreeDto;
import Graduation.CardVisor.domain.serviceThree.ServiceThreeLocations;
import Graduation.CardVisor.domain.serviceone.ServiceOne;
import Graduation.CardVisor.domain.serviceone.ServiceOneCardsDto;
import Graduation.CardVisor.domain.serviceone.ServiceOneDto;
import Graduation.CardVisor.domain.servicetwo.ServiceTwo;
import Graduation.CardVisor.domain.servicetwo.ServiceTwoCardsDto;
import Graduation.CardVisor.domain.servicetwo.ServiceTwoDto;
import Graduation.CardVisor.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class BenefitService {
    private final CardRepository cardRepository;
    private final MemberRepository memberRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final FeeRepository feeRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final ServiceOneRepository serviceOneRepository;
    private final ServiceTwoRepository serviceTwoRepository;
    private final ServiceThreeRepository serviceThreeRepository;
    private final BenefitRepository benefitRepository;


    // ????????? ???!!!!!!!!!!!!!!!!!!!!!!
    // ServiceOneDto ??? ??????????????? <-> ????????? ????????? : ?????? ????????? ??????
    // ServiceOneCardsDto ??? ????????? ????????? <-> ???????????? ????????? : ????????? ?????? ????????? ??????

    private ServiceOneCardsDto resultDto = new ServiceOneCardsDto(); // Flask ???????????? ?????? ??? ???????????? ????????? ?????? dto ??? ??????????????? ??????
    private ServiceTwoCardsDto resultDto2 = new ServiceTwoCardsDto();
    private ServiceThreeCardsDto resultDto3 = new ServiceThreeCardsDto();

    private final CardService cardService;

    // ?????? ????????? 1 : select ?????? -> result ?????? ?????? ???????????? ?????? ???????????? 1????????? 2????????? ?????? ????????? ???????????? ??????????????????.
    public void saveSelections(List<ServiceOneDto> list){ // [{"memberId" : 1, "brandName": transport_bus}, {}, {}...] ???????????? ????????????

        // 1??????: ??????, ????????????????????? ????????? ServiceOneDto ??? ServiceOne ????????? ???????????? ????????????????????? ????????????.
        for(ServiceOneDto serviceOneDto : list){
            DtoToServiceOne(serviceOneDto); // Dto ??? ?????? ServiceOne ????????? ????????????
        }

        // 2??????: Flask ????????? ????????????????????? ????????? ServiceOne ???????????? ???????????? ?????? ??????????????? ??????????????? ??????, ??? ??????(ServiceOneCardsDto ??????)??? ????????????.
        resultDto = flaskServiceOne();
    }

    // ?????? ?????????1 ?????? ?????? : ??????????????? ????????? ServiceOneDto ??? ServiceOne ????????? ???????????? ??????
    public void DtoToServiceOne(ServiceOneDto serviceOneDto) {

        ServiceOne serviceOne = new ServiceOne();

        serviceOne.setMember(memberRepository.getById(serviceOneDto.getMemberId()));
        serviceOne.setBrand(brandRepository.getByNameEngish(serviceOneDto.getBrandName()));

        serviceOneRepository.save(serviceOne);
    }


    // ?????? ????????? 1 ?????? ??????
    // Spring ?????? Flask ??? Fetch ?????? : Flask ??? ??????????????? ???????????? ?????? ?????? ??????, ????????? ?????? ???????????? ???????????? ??????
    // ?????????????????? ??????: Flask ?????? ?????? ??? ?????? ????????? ServiceOneCardsDto ?????? ?????? ????????? ????????????.
    public ServiceOneCardsDto flaskServiceOne() {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:5001/serviceOne")
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<ServiceOneCardsDto>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    // ?????? ?????????1 ?????? ??????: ServiceOneCardsDto ????????? ?????? id??? ???????????? ?????? ?????? ???????????? ?????? ???????????? ??????????????? ??????
    public List<Card> dtoToRecommendedCards() {
        List<Card> cards = new ArrayList<>();

        for (Long cardId : resultDto.getCards()) {
            cards.add(cardRepository.findCardById(cardId));
        }

        return cards;
    }

    // ?????? ????????? 1 ?????? ??????: BestCard ??? ???????????? ????????? ???????????? ?????? ??????????????? ??????
    public List<BenefitDto> bestCardBenefits() {
        return cardService.getBenefits(resultDto.getCards().get(0));
    }


    public Integer bestCardLikeCount() {
        return cardService.getFavoriteCount(resultDto.getCards().get(0));
    }

    // ================================= ????????????

    public ServiceTwoDto flaskServiceTwoSave() {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:5001/serviceTwo/save")
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<ServiceTwoDto>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    public ServiceTwoCardsDto flaskServiceTwoRecommend() {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:5001/serviceTwo/recommend")
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<ServiceTwoCardsDto>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    public ServiceTwoDto DtoToServiceTwo(){

        ServiceTwoDto serviceTwoDto = flaskServiceTwoSave();

        for(ServiceTwoDto.Res res : serviceTwoDto.getRes_list()){
            ServiceTwo serviceTwo =  new ServiceTwo();

            serviceTwo.setBrand(brandRepository.getByNameKorean(res.getPrinted_content()));
            serviceTwo.setMember(memberRepository.getById(1L));
            serviceTwo.setCost(res.getTran_amt());
            serviceTwoRepository.save(serviceTwo);
        }

        return serviceTwoDto;
    }

    public void saveServiceTwo(){
        resultDto2 = flaskServiceTwoRecommend();
    }


    // serviceTwo ???????????? ???????????????, ?????? ?????? ????????? ??????
    public List<Card> dtoToRecommendedCards2(){

        List<Card> cards = new ArrayList<>();
        for(Long cardId : resultDto2.getCards()){
            cards.add(cardRepository.findCardById(cardId));
        }
        return cards;
    }

    // ????????? ?????? ?????? ????????? ??????
    public List<BenefitDto> bestCardBenefits2(){
        return cardService.getBenefits(resultDto2.getCards().get(0));
    }


    // ================================= ???????????????


    public SearchLocalRes kakaoSearchLocal(String query) {
        var uri = UriComponentsBuilder.fromUriString("https://dapi.kakao.com/v2/local/search/keyword.json?query=" + query)
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK 0c332c242e8c1db687c8a561e21704ef");
        headers.setContentType(MediaType.APPLICATION_JSON);
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<SearchLocalRes>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    public ServiceThreeDto flaskServiceThreeSave() {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:5001/serviceThree/save")
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<ServiceThreeDto>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    public ServiceThreeCardsDto flaskServiceThreeRecommend() {
        var uri = UriComponentsBuilder.fromUriString("http://localhost:5001/serviceThree/recommend")
                .build()
                .encode()
                .toUri();

        var headers = new HttpHeaders();
        var httpEntity = new HttpEntity<>(headers);

        var responseType = new ParameterizedTypeReference<ServiceThreeCardsDto>(){};
        var responseEntity = new RestTemplate().exchange(
                uri,
                HttpMethod.GET,
                httpEntity,
                responseType
        );
        return responseEntity.getBody();
    }

    public ServiceThreeDto DtoToServiceThree(){

        ServiceThreeDto serviceThreeDto = flaskServiceThreeSave();

        for(ServiceThreeDto.Res res : serviceThreeDto.getRes_list()){
            ServiceThree serviceThree =  new ServiceThree();

            serviceThree.setBrand(brandRepository.getByNameKorean(res.getPrinted_content()));
            serviceThree.setMember(memberRepository.getById(1L));
            serviceThree.setCost(res.getTran_amt());
            serviceThree.setDate(res.getTran_date());
            serviceThree.setTime(res.getTran_time());

            SearchLocalRes searchLocalRes = kakaoSearchLocal(res.getDetailed());

            serviceThree.setX(Float.parseFloat(searchLocalRes.getDocuments().get(0).getX()));
            serviceThree.setY(Float.parseFloat(searchLocalRes.getDocuments().get(0).getY()));


            serviceThreeRepository.save(serviceThree);
        }

        return serviceThreeDto;
    }

    public ServiceThreeLocations serviceThreeToMap() {

        List<ServiceThree> serviceThreeList = serviceThreeRepository.findAll();

        ServiceThreeLocations serviceThreeLocations = new ServiceThreeLocations();

        List<ServiceThreeLocations.ServiceThreeLocation> locationList = new ArrayList<>();

        for (ServiceThree serviceThree : serviceThreeList) {
            ServiceThreeLocations.ServiceThreeLocation serviceThreeLocation = new ServiceThreeLocations.ServiceThreeLocation();
            serviceThreeLocation.setBrandName(serviceThree.getBrand().getNameEngish());
            serviceThreeLocation.setMapx(serviceThree.getX());
            serviceThreeLocation.setMapy(serviceThree.getY());
            serviceThreeLocation.setCost(serviceThree.getCost());
            serviceThreeLocation.setDate(serviceThree.getDate());
            serviceThreeLocation.setTime(serviceThree.getTime());

            locationList.add(serviceThreeLocation);
        }

        serviceThreeLocations.setLocationList(locationList);

        return serviceThreeLocations;
    }

    public void saveServiceThree(){
        resultDto3 = flaskServiceThreeRecommend();
    }

    // serviceTwo ???????????? ???????????????, ?????? ?????? ????????? ??????
    public List<Card> dtoToRecommendedCards3(){

        List<Card> cards = new ArrayList<>();
        for(Long cardId : resultDto3.getCards()){
            cards.add(cardRepository.findCardById(cardId));
        }
        return cards;
    }

    // ????????? ?????? ?????? ????????? ??????
    public List<BenefitDto> bestCardBenefits3(){
        return cardService.getBenefits(resultDto3.getCards().get(0));
    }
}
