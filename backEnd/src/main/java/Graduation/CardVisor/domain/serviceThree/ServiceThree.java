package Graduation.CardVisor.domain.serviceThree;

import Graduation.CardVisor.domain.Brand;
import Graduation.CardVisor.domain.Member;
import lombok.Data;

import javax.persistence.*;

@Entity(name = "servicethree")
@Data
public class ServiceThree {

    @Id
    @GeneratedValue
    @Column(name = "servicethree_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(name = "cost")
    private Integer cost;

    @Column(name = "x")
    private Float x;

    @Column(name = "y")
    private Float y;

    @Column(name = "date")
    private String date;

    @Column(name = "time")
    private String time;
}
