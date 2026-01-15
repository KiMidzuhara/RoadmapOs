package com.midzuhara.roadmapos.graph.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "roadmaps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Roadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "roadmap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Node> nodes = new ArrayList<>();

    @OneToMany(mappedBy = "roadmap",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Edge> edges = new ArrayList<>();

    public void addNode(Node node) {
        nodes.add(node);
        node.setRoadmap(this);
    }
    public void removeNode(Node node) {
        nodes.remove(node);
        node.setRoadmap(null);
    }

    public void addEdge(Edge edge) {
        edges.add(edge);
        edge.setRoadmap(this);
    }
    public void removeEdge(Edge edge) {
        edges.remove(edge);
        edge.setRoadmap(null);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Roadmap roadmap = (Roadmap) o;
        return getId() != null && Objects.equals(getId(), roadmap.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }
}
