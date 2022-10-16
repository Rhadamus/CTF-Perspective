#ifdef GL_ES
	precision mediump float;
#endif

varying mediump vec2 texCoordinate;
uniform sampler2D texture;

uniform lowp int inkEffect;
uniform lowp vec4 blendColor;

uniform float fB;
uniform lowp int pDir;

void main()
{
	vec2 posTex;
	float fC;
	
	if(pDir == 0)
	{
		fC =  max(0.02, 1.0+(fB - 1.0)*4.0*pow((texCoordinate.s-0.5),2.0));
		posTex = texCoordinate * vec2(1.0, fC) + vec2(0.0, (1.0-fC)/2.0);
	}
	else
	{
		fC =  max(0.05, 1.0+(fB - 1.0)*4.0*pow((texCoordinate.t-0.5),2.0));
		posTex = texCoordinate * vec2(fC, 1.0) + vec2((1.0-fC)/2.0, 0.0);
	}
	
	gl_FragColor = texture2D(texture, posTex) * blendColor;
}
