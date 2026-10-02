package com.digitalheroes.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="charities")
public class Charity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false) String name;
    @Column(nullable=false,unique=true) String slug;
    @Column(columnDefinition="text") String description;
    @Column(name="logo_url") String logoUrl;
    @Column(name="is_featured",nullable=false) boolean featured;
    @Column(name="is_archived",nullable=false) boolean archived;
    @Column(name="created_at",nullable=false) Instant createdAt=Instant.now();
    @Column(name="updated_at",nullable=false) Instant updatedAt=Instant.now();
    public Charity() {}
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public String getName(){return name;} public void setName(String name){this.name=name;}
    public String getSlug(){return slug;} public void setSlug(String slug){this.slug=slug;}
    public String getDescription(){return description;} public void setDescription(String description){this.description=description;}
    public String getLogoUrl(){return logoUrl;} public void setLogoUrl(String logoUrl){this.logoUrl=logoUrl;}
    public boolean isFeatured(){return featured;} public void setFeatured(boolean featured){this.featured=featured;}
    public boolean isArchived(){return archived;} public void setArchived(boolean archived){this.archived=archived;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant createdAt){this.createdAt=createdAt;}
    public Instant getUpdatedAt(){return updatedAt;} public void setUpdatedAt(Instant updatedAt){this.updatedAt=updatedAt;}
}
