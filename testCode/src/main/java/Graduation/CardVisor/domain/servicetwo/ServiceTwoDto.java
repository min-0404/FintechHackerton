package Graduation.CardVisor.domain.servicetwo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceTwoDto {

    private Long memberId;
    private String brandName;
    private Date date;
    private Integer cost;

}
