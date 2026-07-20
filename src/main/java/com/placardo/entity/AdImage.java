package com.placardo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ad_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ad_id")
    private Ad ad;

    /** Имя файла на диске (uuid.jpg), без пути */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;
}
