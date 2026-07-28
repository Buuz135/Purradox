#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>

in vec2 portalPos;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

out vec4 fragColor;

float hash21(vec2 cell) {
    return fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453);
}

void main() {

    vec2 gridPos = floor(portalPos * 48.0) / 48.0;
    vec2 p = (gridPos - 0.5) * 2.0;
    float radius = length(p);

    if (radius > 0.98) {
        discard;
    }

    float time = GameTime * 1200.0;
    float angle = atan(p.y, p.x);
    float tunnel = angle * 5.0 - log(max(radius, 0.035)) * 9.0 - time * 1.8;
    float spiral = 0.5 + 0.5 * sin(tunnel);
    float counterSpiral = 0.5 + 0.5 * sin(angle * -3.0 - radius * 34.0 + time * 2.7);
    float ring = 0.5 + 0.5 * sin(radius * 52.0 - time * 4.2 + angle * 2.0);

    vec3 deepVoid = vec3(0.015, 0.018, 0.055);
    vec3 temporalCyan = vec3(0.08, 0.82, 0.88);
    vec3 paradoxMagenta = vec3(0.72, 0.16, 0.66);
    vec3 cargoAmber = vec3(1.0, 0.58, 0.16);

    float depthShade = smoothstep(0.08, 0.92, radius);
    float bands = smoothstep(0.46, 0.72, spiral) * (0.28 + ring * 0.72);
    vec3 color = mix(deepVoid * 0.25, deepVoid, depthShade);
    color += temporalCyan * bands * (0.25 + depthShade * 0.9);
    color += paradoxMagenta * smoothstep(0.68, 0.9, counterSpiral) * depthShade * 0.72;

    vec2 cell = floor(portalPos * 48.0);
    float spark = step(0.968, hash21(cell + floor(time * 1.5)));
    spark *= smoothstep(0.18, 0.9, radius);


    float eventHorizon = smoothstep(0.76, 0.82, radius) * (1.0 - smoothstep(0.91, 0.96, radius));
    color += mix(temporalCyan, cargoAmber, 0.35 + 0.35 * sin(angle * 8.0 - time * 3.0)) * eventHorizon * 1.4;

    float core = 1.0 - smoothstep(0.02, 0.2, radius);
    color = mix(color, vec3(0.003, 0.002, 0.012), core);


    color = floor(color * 12.0) / 12.0;
    vec4 wormhole = vec4(color, 1.0 - smoothstep(0.91, 0.99, radius));
    fragColor = apply_fog(
        wormhole,
        sphericalVertexDistance,
        cylindricalVertexDistance,
        FogEnvironmentalStart,
        FogEnvironmentalEnd,
        FogRenderDistanceStart,
        FogRenderDistanceEnd,
        FogColor
    );
}
