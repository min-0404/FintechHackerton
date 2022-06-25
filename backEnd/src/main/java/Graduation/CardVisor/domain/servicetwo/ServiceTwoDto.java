package Graduation.CardVisor.domain.servicetwo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTwoDto {

    private String bank_name;
    private List<Res> res_list;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Res {
        private String tran_date;
        private String tran_time;
        private String printed_content;
        private Integer tran_amt;
    }

//    "tran_date": "20220104",
//            "tran_time": "075344",
//            "printed_content": "삼성페이",
//            "tran_amt": "14490"
}
