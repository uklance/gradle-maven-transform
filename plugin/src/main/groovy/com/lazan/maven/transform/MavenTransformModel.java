package com.lazan.maven.transform;

import org.gradle.api.Action;

public interface MavenTransformModel {
    void rootPom(Object rootPom);
    void transformManyToOne(Action<? super ManyToOneModel> action);
    void transformOneToOne(Action<? super OneToOneModel> action);
}
