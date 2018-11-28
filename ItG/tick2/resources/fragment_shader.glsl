#version 140


in vec3 frag_normal;	    // fragment normal in world space
in vec2 frag_texcoord;		// fragment texture coordinates in texture space

out vec3 pixel_colour;

uniform sampler2D tex;  // 2D texture sampler

// Tone mapping and display encoding combined
vec3 tonemap( vec3 linearRGB )
{
    float L_white = 0.7; // Controls the brightness of the image

    float inverseGamma = 1./2.2;
    return pow( linearRGB/L_white, vec3(inverseGamma) ); // Display encoding - a gamma
}

void main()
{
	const vec3 I_a = vec3(0.01, 0.01, 0.01);       // Ambient light intensity (and colour)

	const float k_d = 0.6;                      // Diffuse light factor
    vec3 C_diff = texture(tex, frag_texcoord).rgb;    // Diffuse material colour (TODO: replace with texture)

	const vec3 I = vec3(0.941, 0.968, 1);   // Light intensity (and colour)
	vec3 L = normalize(vec3(2, 1.5, -0.5)); // The light direction as a unit vector
	vec3 N = frag_normal;                   // Normal in world coordinates


    vec3 linear_colour = C_diff * I_a + C_diff * k_d * I * max(dot(N, L), 0.0);

    pixel_colour = tonemap(linear_colour);
}
