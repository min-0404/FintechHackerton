package Graduation.CardVisor.repository;

import Graduation.CardVisor.domain.servicetwo.ServiceTwo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTwoRepository extends JpaRepository<ServiceTwo, Long> {

    public List<ServiceTwo> findAllByMemberId(Long id);
}
