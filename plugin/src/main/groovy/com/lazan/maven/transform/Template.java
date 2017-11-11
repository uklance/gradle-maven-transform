package com.lazan.maven.transform;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * Created by Lance on 11/11/2017.
 */
public interface Template {
    void transform(Map<String, Object> context, OutputStream out) throws IOException;
}
