package hu.kits.tennis.infrastructure.web.api;

import java.io.StringWriter;
import java.util.Map;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import io.javalin.http.Context;
import io.javalin.rendering.FileRenderer;

class MustacheFileRenderer implements FileRenderer {

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    @Override
    public String render(String filePath, Map<String, ?> model, Context context) {
        Mustache mustache = mustacheFactory.compile(filePath);
        StringWriter writer = new StringWriter();
        mustache.execute(writer, model);
        return writer.toString();
    }

}
