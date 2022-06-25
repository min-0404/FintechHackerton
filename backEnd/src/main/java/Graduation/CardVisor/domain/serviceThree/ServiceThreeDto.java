package Graduation.CardVisor.domain.serviceThree;

import Graduation.CardVisor.domain.servicetwo.ServiceTwoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceThreeDto {

    private String bank_name;
    private List<ServiceThreeDto.Res> res_list;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Res {
        private String tran_date;
        private String tran_time;
        private String printed_content;
        private String detailed;
        private Integer tran_amt;
    }
}
