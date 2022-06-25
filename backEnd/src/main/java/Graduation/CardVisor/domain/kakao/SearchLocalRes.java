package Graduation.CardVisor.domain.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchLocalRes {


    private Meta meta;
    private List<Documents> documents;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {

        private SameName same_name;
        private Integer pageable_count;
        private Integer total_count;
        private Boolean is_end;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SameName {
            private List<String> region;
            private String keyword;
            private String selected_region;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Documents {
        private String place_name;
        private String distance;
        private String place_url;
        private String category_name;
        private String address_name;
        private String road_address_name;
        private String id;
        private String phone;
        private String category_group_code;
        private String category_group_name;
        private String x;
        private String y;
    }
}
