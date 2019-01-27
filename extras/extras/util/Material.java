package util;

import org.joml.*;

import java.util.Vector;

/**
 * This class represents material. Material is represented using (a) ambient (b)
 * diffuse (c) specular (d) emission (for materials that emit light themselves)
 * It also has coefficients for shininess, absorption, reflection (for
 * reflective material), transparency (for transparent material) and refractive
 * index (for transparent material). For the latter ones, the user must make
 * sure that absorption + reflection + transparency = 1
 */
public class Material {
  private Vector4f emission;
  private Vector4f ambient;
  private Vector4f diffuse;
  private Vector4f specular;
  private float shininess;
  private float absorption, reflection, transparency, refractive_index;

  public Material() {
    emission = new Vector4f();
    ambient = new Vector4f();
    diffuse = new Vector4f();
    specular = new Vector4f();
    init();
  }

  public Material(Material mat) {
    emission = new Vector4f(mat.getEmission());
    ambient = new Vector4f(mat.getAmbient());
    diffuse = new Vector4f(mat.getDiffuse());
    specular = new Vector4f(mat.getSpecular());
    this.setShininess(mat.getShininess());
    this.setAbsorption(mat.getAbsorption());
    this.setReflection(mat.getReflection());
    this.setTransparency(mat.getTransparency());
    this.setRefractiveIndex(mat.getRefractiveIndex());
  }


  public void init() {
    setEmission(0.0f, 0.0f, 0.0f);
    setAmbient(0.0f, 0.0f, 0.0f);
    setDiffuse(0.0f, 0.0f, 0.0f);
    setSpecular(0.0f, 0.0f, 0.0f);
    setShininess(0.0f);
    setAbsorption(1);
    setReflection(0);
    setTransparency(0);
  }

  public void setEmission(float r, float g, float b) {
    emission.x = r;
    emission.y = g;
    emission.z = b;
    emission.w = 1;
  }

  public void setEmission(Vector4f v) {
    emission = new Vector4f(v);
  }

  public void setAmbient(float r, float g, float b) {
    ambient.x = r;
    ambient.y = g;
    ambient.z = b;
    ambient.w = 1;
  }

  public void setAmbient(Vector4f v) {
    ambient = new Vector4f(v);
  }

  public void setDiffuse(float r, float g, float b) {
    diffuse.x = r;
    diffuse.y = g;
    diffuse.z = b;
    diffuse.w = 1;
  }

  public void setDiffuse(Vector4f v) {
    diffuse = new Vector4f(v);
  }

  public void setSpecular(float r, float g, float b) {
    specular.x = r;
    specular.y = g;
    specular.z = b;
    specular.w = 1;
  }

  public void setSpecular(Vector4f v) {
    specular = new Vector4f(v);
  }

  public void setShininess(float r) {
    shininess = r;
  }

  public void setAbsorption(float a) {
    absorption = a;
  }

  public void setReflection(float r) {
    reflection = r;
  }

  public void setTransparency(float t) {
    transparency = t;
    ambient.w = diffuse.w = specular.w = emission.w = 1 - t;
  }

  public void setRefractiveIndex(float r) {
    refractive_index = r;
  }

  public Vector4f getEmission() {
    return emission;
  }

  public Vector4f getAmbient() {
    return ambient;
  }

  public Vector4f getDiffuse() {
    return diffuse;
  }

  public Vector4f getSpecular() {
    return specular;
  }

  public float getShininess() {
    return shininess;
  }

  public float getAbsorption() {
    return absorption;
  }

  public float getReflection() {
    return reflection;
  }

  public float getTransparency() {
    return transparency;
  }

  public float getRefractiveIndex() {
    return refractive_index;
  }
}
