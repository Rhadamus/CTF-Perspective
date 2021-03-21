precision highp float;

varying mediump vec2 texCoordinate;
uniform sampler2D texture;

uniform lowp float inkParam;
uniform lowp int inkEffect;
uniform lowp vec3 rgbCoeff;

uniform float Zoom;
uniform float WaveIncrement;
uniform float Offset;
uniform int Mode;

uniform vec2 scale;
uniform vec2 offset;
uniform int pDir;

//float delta = (float)(3.141592/180.0);
#define delta 3.141592/180.0

void main()
{
    lowp vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
    mediump vec2 posTex;

    if(pDir != 0)
    {
        float y= texCoordinate.y;
        if(Mode == 0)
            y = (1.0-y);

        float ScreenX = 1.0 + sin((y*WaveIncrement+Offset)*delta)*Zoom-Zoom;
        posTex = texCoordinate * vec2(ScreenX, 1.0) + vec2((1.0-ScreenX)/2.0, 0.0);
        color = texture2D(texture, posTex*scale+offset);
    }
    else
    {
        float ScreenY = 1.0 + sin((texCoordinate.x*WaveIncrement+Offset)*delta)*Zoom-Zoom;
        posTex = texCoordinate * vec2(1.0, ScreenY) + vec2(0.0, (1.0-ScreenY)/2.0);
        color = texture2D(texture, posTex*scale+offset);
    }
    color = color * vec4(rgbCoeff, inkParam);

    if(inkEffect == 2)			//INVERT
    color.rgb = vec3(1,1,1)-color.rgb;
    else if(inkEffect == 10)	//MONO
    {
        lowp float mono = 0.3125*color.r + 0.5625*color.g + 0.125*color.b;
        color.rgb = vec3(mono,mono,mono);
    }

    gl_FragColor = color;
}