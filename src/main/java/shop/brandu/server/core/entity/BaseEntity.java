package shop.brandu.server.core.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

/**
 * 생성일, 수정일등의 공통적인 필드를 가지는 슈퍼 타입의 엔티티 클래스 <br/>
 *
 * @author : sunsuking
 * @fileName : BaseEntity
 * @since : 4/17/24
 */
@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
