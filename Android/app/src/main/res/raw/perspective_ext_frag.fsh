precision highp float;

varying mediump vec2 texCoordinate;
uniform sampler2D texture;

uniform lowp float inkParam;
uniform lowp int inkEffect;
uniform lowp vec3 rgbCoeff;

uniform float fA;
uniform float fB;

uniform int pDir;

void main()
{
	mediump vec2 posTex;

	if(pDir != 0)
	{
    	float ScreenX = (fA-(fA-fB)*(1.0-texCoordinate.y));
		posTex = texCoordinate * vec2(ScreenX, 1.0) + vec2((1.0-ScreenX)/2.0, 0.0);
	}
	else
	{
		float ScreenY = (fA-(fA-fB)*texCoordinate.x);
		posTex = texCoordinate * vec2(1.0, ScreenY) + vec2(0.0, (1.0-ScreenY)/2.0);
	}
	
	gl_FragColor = texture2D(texture, posTex) * vec4(rgbCoeff, inkParam);
}