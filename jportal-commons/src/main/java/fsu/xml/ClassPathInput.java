package fsu.xml;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

public class ClassPathInput implements LSInput {
    InputStream input;

    public ClassPathInput(InputStream input) {
        setByteStream(input);
    }

    @Override
    public Reader getCharacterStream() {
        return new InputStreamReader(getByteStream());
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
    }

    @Override
    public InputStream getByteStream() {
        return input;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        this.input = byteStream;
    }

    @Override
    public String getStringData() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setStringData(String stringData) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getSystemId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setSystemId(String systemId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getPublicId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPublicId(String publicId) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setBaseURI(String baseURI) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getEncoding() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setEncoding(String encoding) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean getCertifiedText() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
        // TODO Auto-generated method stub

    }

}