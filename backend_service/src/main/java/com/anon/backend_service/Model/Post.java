package com.anon.backend_service.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name = "posts")
public class Post {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false, length = 1000)
    private String content;
    
    // Only linked to anonymous ID - never to user's actual account ID
    @Column(nullable = false)
    private String anonymousId;
    
    // Optional: Display name that can be rotated/changed
    private String postDisplayName;
    
    private int likeCount = 0;
    private int commentCount = 0;
    
    private boolean isActive = true;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}