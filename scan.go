package main

import (
	"errors"

	"github.com/kbinani/screenshot"
	"github.com/makiuchi-d/gozxing"
	"github.com/makiuchi-d/gozxing/oned"
	"github.com/makiuchi-d/gozxing/qrcode"
)

// Scans the screen (all available) if it contains any supported barcode and tries to decode it.
// Returns a successfully decoded barcode value or an error.
func ScanScreenBarcode() (string, error) {
	n := screenshot.NumActiveDisplays()

	var lastErr error

	for i := 0; i < n; i++ {
		bounds := screenshot.GetDisplayBounds(i)

		img, err := screenshot.CaptureRect(bounds)
		if err != nil {
			panic(err)
		}

		bmp, err := gozxing.NewBinaryBitmapFromImage(img)
		if err != nil {
			panic(err)
		}

		hints := map[gozxing.DecodeHintType]interface{}{gozxing.DecodeHintType_TRY_HARDER: true}

		reader := oned.NewCodaBarReader()
		result, err := reader.Decode(bmp, hints)
		if err == nil {
			return result.GetText(), nil
		}
		reader = oned.NewCode128Reader()
		result, err = reader.Decode(bmp, hints)
		if err == nil {
			return result.GetText(), nil
		}
		reader = oned.NewCode39Reader()
		result, err = reader.Decode(bmp, hints)
		if err == nil {
			return result.GetText(), nil
		}
		reader = oned.NewMultiFormatUPCEANReader(hints)
		result, err = reader.Decode(bmp, hints)
		if err == nil {
			return result.GetText(), nil
		}
		reader = qrcode.NewQRCodeReader()
		result, err = reader.Decode(bmp, hints)
		if err == nil {
			return result.GetText(), nil
		}
		lastErr = err
	}
	if lastErr != nil {
		return "", lastErr
	}
	return "", errors.New("could not find a barcode")
}
