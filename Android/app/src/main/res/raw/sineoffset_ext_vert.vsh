attribute vec4 position;
attribute vec2 texCoord;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;

varying vec2 texCoordinate;

void main()
{
    texCoordinate = texCoord;
    gl_Position = projectionMatrix * position;
}