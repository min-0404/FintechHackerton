package Graduation.CardVisor.domain.serviceThree;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceThreeLocations {

    private List<ServiceThreeLocation> locationList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceThreeLocation {

        private String brandName;
        private Float mapx;
        private Float mapy;
        private String date;
        private String time;
        private Integer cost;

    }
}
