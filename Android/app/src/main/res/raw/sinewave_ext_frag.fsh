precision highp float;

varying mediump vec2 texCoordinate;
uniform sampler2D texture;

uniform lowp float inkParam;
uniform lowp int inkEffect;
uniform lowp vec3 rgbCoeff;

uniform float Zoom;
uniform float WaveIncrement;
uniform float Offset;

uniform int pDir;

//float delta = (float)(3.141592/180.0);
#define delta 3.141592/180.0

void main()
{
    mediump vec2 posTex;

    if(pDir != 0)
    {
        float y= 1.0 - texCoordinate.y;

        float ScreenX = 1.0 + sin((y*WaveIncrement+Offset)*delta)*Zoom-Zoom;
        posTex = texCoordinate * vec2(ScreenX, 1.0) + vec2((1.0-ScreenX)/2.0, 0.0);
    }
    else
    {
        float ScreenY = 1.0 + sin((texCoordinate.x*WaveIncrement+Offset)*delta)*Zoom-Zoom;
        posTex = texCoordinate * vec2(1.0, ScreenY) + vec2(0.0, (1.0-ScreenY)/2.0);
    }

    gl_FragColor = texture2D(texture, posTex) * vec4(rgbCoeff, inkParam);
}