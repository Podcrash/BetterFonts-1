package betterfonts;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class BetterFontRendererFactoryImpl implements BetterFontRendererFactory
{
    private final OglService oglService;
    private final int[] colors;
    private final List<Font> fonts = new ArrayList<>();

    public BetterFontRendererFactoryImpl(OglService oglService, int[] colors)
    {
        this.oglService = oglService;
        this.colors = colors;
    }

    @Override
    public BetterFontRendererFactory useSystemFonts(int pointSize)
    {
        /* Use Java's logical font as the default initial font if user does not override it */
        GraphicsEnvironment.getLocalGraphicsEnvironment().preferLocaleFonts();
        fonts.add(new OpenTypeFont(oglService, new java.awt.Font(java.awt.Font.SANS_SERIF, Font.PLAIN, pointSize), Baseline.MINECRAFT));
        fonts.addAll(Arrays.stream(GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                .map(f -> new OpenTypeFont(oglService, f.deriveFont(f.getStyle(), pointSize), Baseline.MINECRAFT))
                .collect(Collectors.toList()));
        return this;
    }

    @Override
    public BetterFontRendererFactory withOpenTypeFont(String name, Function<AwtBuilder, AwtBuilderEnd> openTypeFont)
    {
        fonts.add(((AwtBuilderImpl) openTypeFont.apply(new AwtBuilderImpl(name))).getFont());
        return this;
    }

    @Override
    public BetterFontRendererFactory withOpenTypeFont(Supplier<InputStream> is, Function<AwtBuilder, AwtBuilderEnd> openTypeFont)
    {
        withAwtFont(is, java.awt.Font.TRUETYPE_FONT, openTypeFont);
        return this;
    }

    @Override
    public BetterFontRendererFactory withAwtFont(Supplier<InputStream> is, int fontFormat, Function<AwtBuilder, AwtBuilderEnd> openTypeFont)
    {
        fonts.add(((AwtBuilderImpl) openTypeFont.apply(new AwtBuilderImpl(is, fontFormat))).getFont());
        return this;
    }

    @Override
    public BetterFontRendererFactory withBitmapAsciiFont(String name, Supplier<InputStream> bitmap, int size)
    {
        fonts.add(new BitmapAsciiFont(oglService, bitmap, name, Font.PLAIN, size));
        return this;
    }

    @Override
    public BetterFontRendererFactory withBitmapUnifont(String name, Supplier<InputStream> glyphSizes, IntFunction<InputStream> pageSupplier, int size)
    {
        fonts.add(new BitmapUnifont(oglService, glyphSizes, pageSupplier, name, Font.PLAIN, size));
        return this;
    }

    @Override
    public BetterFontRenderer build()
    {
        final BetterFontRenderer fontRenderer = new BetterFontRenderer(oglService, colors, fonts, true);
        fonts.clear();
        return fontRenderer;
    }

    private class AwtBuilderImpl implements AwtBuilder, AwtBuilderEnd
    {
        private final String name;
        private final Supplier<InputStream> inputStream;
        private final Integer inputStreamFormat;

        private int size;
        private Baseline baseline = Baseline.MINECRAFT;
        private float customBaseline;

        public AwtBuilderImpl(String name)
        {
            this.name = name;
            this.inputStream = null;
            this.inputStreamFormat = null;
        }

        public AwtBuilderImpl(Supplier<InputStream> inputStream, int inputStreamFormat)
        {
            this.inputStream = inputStream;
            this.inputStreamFormat = inputStreamFormat;
            this.name = null;
        }

        @Override
        public AwtBuilderEnd fromPointSize(int pointSize)
        {
            this.size = pointSize;
            return this;
        }

        @Override
        public AwtBuilderEnd fromHeight(float height)
        {
            final FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
            java.awt.Font font = getAwtFont((int) height);
            Rectangle2D rectangle = null;

            int pointSize = (int) height;
            int tries = 0;
            do {
                if(rectangle != null)
                    pointSize += (int) rectangle.getHeight() < height ? 1 : -1;

                font = font.deriveFont(java.awt.Font.PLAIN, pointSize);
                rectangle = font.createGlyphVector(frc, Constants.COMMON_CHARS).getVisualBounds();
            } while(Math.abs(rectangle.getHeight() - height) <= 0.01f && ++tries < 10);

            this.size = pointSize;
            return this;
        }

        @Override
        public AwtBuilderEnd withBaseline(float baseline)
        {
            this.baseline = null;
            this.customBaseline = baseline;
            return this;
        }

        @Override
        public AwtBuilderEnd withBaseline(Baseline baseline)
        {
            this.baseline = baseline;
            return this;
        }

        private java.awt.Font getAwtFont(int size)
        {
            if(name != null)
                return new java.awt.Font(name, Font.PLAIN, size);

            if(inputStream != null && inputStreamFormat != null)
            {
                try(InputStream is = this.inputStream.get())
                {
                    return java.awt.Font.createFont(inputStreamFormat, is).deriveFont(Font.PLAIN, size);
                }
                catch(FontFormatException ex)
                {
                    throw new RuntimeException("Invalid font format for the provided InputStream", ex);
                }
                catch(IOException ex)
                {
                    throw new UncheckedIOException("Couldn't read the provided font InputStream", ex);
                }
            }

            throw new AssertionError("Invalid OpenTypeBuilderImpl state");
        }

        public Font getFont()
        {
            if(baseline == null)
                return new OpenTypeFont(oglService, getAwtFont(size), customBaseline);
            return new OpenTypeFont(oglService, getAwtFont(size), baseline);
        }
    }
}
