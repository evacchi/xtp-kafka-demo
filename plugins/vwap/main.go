// Note: run `go doc -all` in this package to see all of the types and functions available.
// ./pdk.gen.go contains the domain types from the host where your plugin will run.
package main

import (
	"github.com/cinar/indicator"
	"github.com/extism/go-pdk"
)

const period = 10

type window struct {
	prices, volumes [period]float64
	index, count    uint
}

var (
	w     = &window{prices: [period]float64{}, volumes: [period]float64{}}
	topic = "vwap-output"
)

func (w *window) append(o Order) {
	w.count++
	w.index = w.count % period
	w.prices[w.index] = o.Price
	w.volumes[w.index] = float64(o.Volume)
}

func (w *window) ready() bool {
	return w.count >= period
}

func (w *window) vwap() float64 {
	return indicator.VolumeWeightedAveragePrice(period, w.prices[:], w.volumes[:])[period-1]
}

func init() {
	if name, ok := pdk.GetConfig("topic-name"); ok {
		topic = name
	}
}

func Transform(input Record) ([]Record, error) {
	w.append(input.Value)
	if w.ready() {
		input.Topic = topic
		input.Value.Price = w.vwap()
		input.Value.Volume = 0
		return []Record{input}, nil
	} else {
		return []Record{}, nil
	}
}
