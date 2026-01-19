package io.github.elpis.reactive.websockets.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ChainRenderer {

  private ChainRenderer() {}

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static final class Builder<T> {

    private final List<Section<T>> sections = new ArrayList<>();
    private Function<T, String> nameResolver = t -> t.getClass().getSimpleName();
    private String title = "Chain Configuration";
    private int minWidth = 60;

    public Builder<T> title(final String title) {
      this.title = title;
      return this;
    }

    public Builder<T> nameResolver(final Function<T, String> resolver) {
      this.nameResolver = resolver;
      return this;
    }

    public Builder<T> minWidth(final int width) {
      this.minWidth = width;
      return this;
    }

    public Builder<T> section(final String name, final List<T> items) {
      this.sections.add(new Section<>(name, items));
      return this;
    }

    public void render(final Consumer<String> renderer) {
      renderer.accept(new Renderer<>(title, sections, nameResolver, minWidth).render());
    }
  }

  private record Section<T>(String name, List<T> items) {}

  private record Renderer<T>(
      String title, List<Section<T>> sections, Function<T, String> nameResolver, int minWidth) {
    String render() {
      final int width = calculateWidth();

      StringBuilder sb = new StringBuilder(512);
      sb.append(renderTitle(width)).append('\n');

      for (Section<T> section : sections) {
        sb.append(renderSection(section, width)).append('\n');
      }

      return sb.toString();
    }

    private int calculateWidth() {
      int max = title.length();

      for (Section<T> section : sections) {
        max = Math.max(max, section.name.length());
        for (int i = 0; i < section.items.size(); i++) {
          String name = indexedName(i, section.items.get(i));
          max = Math.max(max, name.length());
        }
      }

      return Math.max(max + 4, minWidth);
    }

    private String renderTitle(final int width) {
      return "╔"
          + "═".repeat(width + 2)
          + "╗\n"
          + "║ "
          + center(title, width)
          + " ║\n"
          + "╚"
          + "═".repeat(width + 2)
          + "╝";
    }

    private String renderSection(final Section<T> section, final int width) {
      StringBuilder sb = new StringBuilder();

      sb.append("┌─ ")
          .append(section.name)
          .append(" ")
          .append("─".repeat(width - section.name.length() - 1))
          .append("┐\n");

      if (section.items.isEmpty()) {
        sb.append("│ ").append(pad("<empty>", width)).append(" │\n");
      } else {
        for (int i = 0; i < section.items.size(); i++) {
          sb.append("│ ").append(pad(indexedName(i, section.items.get(i)), width)).append(" │\n");

          if (i < section.items.size() - 1) {
            sb.append("│ ").append(pad("  ↓", width)).append(" │\n");
          }
        }
      }

      sb.append("└").append("─".repeat(width + 2)).append("┘\n");
      return sb.toString();
    }

    private String indexedName(final int index, final T item) {
      return (index + 1) + ". " + nameResolver.apply(item);
    }
  }

  private static String pad(final String s, final int width) {
    return s.length() >= width ? s.substring(0, width) : s + " ".repeat(width - s.length());
  }

  private static String center(final String s, final int width) {
    if (s.length() >= width) return s.substring(0, width);
    int left = (width - s.length()) / 2;
    return " ".repeat(left) + s + " ".repeat(width - s.length() - left);
  }
}
