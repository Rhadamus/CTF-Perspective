#ifdef GL_ES
#if defined(GL_FRAGMENT_PRECISION_HIGH) || defined(GL_OES_standard_derivatives)
precision highp float;
#else
precision mediump float;
#endif
#endif

varying mediump vec2 texCoordinate;
uniform sampler2D texture;

uniform lowp int inkEffect;
uniform lowp vec4 blendColor;

uniform float Zoom;
uniform float WaveIncrement;
uniform float Offset;
uniform lowp int pDir;

#define delta 3.141592/180.0

void main() {
    mediump vec2 posTex;

    if (pDir == 0) {
        mediump float y = 1.0 - texCoordinate.y;

        float ScreenX = 1.0 + sin((y*WaveIncrement+Offset)*delta)*Zoom;
        posTex = (texCoordinate + vec2((1.0-ScreenX)/2.0, 0.0));
    } else {
        mediump float x = texCoordinate.x*WaveIncrement+Offset;

        float ScreenY = 1.0 - sin(x*delta)*Zoom;
        posTex = (texCoordinate+ vec2(0.0, (2.0-2.0*ScreenY)/2.0));
    }

    gl_FragColor = texture2D(texture, posTex);
}
